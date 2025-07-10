package com.moogsan.moongsan_backend.domain.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusClosedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderPendingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.GroupBuyEventMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.OrderEventMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.domain.chatting.participant.Facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.policy.DueSoonPolicy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.dto.request.OrderCreateRequest;
import com.moogsan.moongsan_backend.domain.order.dto.response.OrderCreateResponse;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.GROUPBUY_STATUS_CLOSED;
import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.ORDER_STATUS_PENDING;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreateService {

    private final UserRepository userRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final DueSoonPolicy dueSoonPolicy;
    private final ChattingCommandFacade chattingCommandFacade;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final OrderEventMapper orderEventMapper;
    private final GroupBuyEventMapper groupBuyEventMapper;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long userId) {
        // 1. 유저 및 공동구매 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));
        GroupBuy groupBuy = groupBuyRepository.findById(request.getPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "공동구매 정보를 찾을 수 없습니다."));

        // 2. 상태 및 중복 검사
        if (!"OPEN".equals(groupBuy.getPostStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "현재 주문이 불가능한 상태입니다.");
        }
        int canceledCount = orderRepository.countByUserIdAndGroupBuyIdAndStatusIn(
                userId, request.getPostId(), List.of("CANCELED", "REFUNDED")
        );
        if (canceledCount > 3) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "주문을 3회 이상 취소하였습니다.");
        }
        if (orderRepository.existsByUserIdAndGroupBuyIdAndStatusIn(userId, request.getPostId(), List.of("CANCELED"))) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "환불중인 주문이 존재합니다.");
        }
        if (orderRepository.existsByUserIdAndGroupBuyIdAndStatusNotIn(userId, request.getPostId(), List.of("CANCELED", "REFUNDED"))) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "이미 공동구매에 참여하였습니다.");
        }
        if (request.getQuantity() % groupBuy.getUnitAmount() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "수량은 주문 단위의 배수여야 합니다.");
        }

        // 3. 주문 엔티티 생성
        Order order = Order.builder()
                .user(user)
                .groupBuy(groupBuy)
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .name(request.getName() != null ? request.getName() : user.getName())
                .build();

        String stockKey = "order:groupbuy:stock:" + request.getPostId();
        String orderCheckKey = "order:user:" + userId + ":groupbuy:" + request.getPostId();
        String lockKey = "order:lock:groupbuy:" + request.getPostId();
        RLock lock = redissonClient.getLock(lockKey);

        // 4. Redis 잠금 및 재고 감소
        try {
            if (!lock.tryLock(3, 2, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "잠시 후 다시 시도해주세요.");
            }
            Boolean isNew = redisTemplate.opsForValue()
                    .setIfAbsent(orderCheckKey, "1", Duration.ofMinutes(10));
            if (Boolean.FALSE.equals(isNew)) {
                throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "이미 공동구매에 참여하였습니다.");
            }
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(
                    "local stock = redis.call('get', KEYS[1]);" +
                            "if (stock and tonumber(stock) >= tonumber(ARGV[1])) then return redis.call('decrby', KEYS[1], ARGV[1]); else return -1; end"
            );
            script.setResultType(Long.class);
            Long result = redisTemplate.execute(script, List.of(stockKey), String.valueOf(request.getQuantity()));
            if (result == null || result < 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "남은 수량을 초과하여 주문할 수 없습니다.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "락 획득 중 오류가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }

        // 5. DB 업데이트 및 채팅방 조인
        groupBuy.decreaseLeftAmount(request.getQuantity());
        groupBuy.increaseParticipantCount();
        groupBuy.updateDueSoonStatus(dueSoonPolicy);
        orderRepository.save(order);
        chattingCommandFacade.joinChatRoom(user, groupBuy.getId());

        // 6. CLOSED 이벤트 (마감 시)
        if (groupBuy.getLeftAmount() == 0) {
            groupBuy.changePostStatus("CLOSED");
            List<Long> participantIds = orderRepository
                    .findAllByGroupBuyIdOrderByStatusCustom(groupBuy.getId())
                    .stream().map(o -> o.getUser().getId()).distinct().toList();
            try {
                GroupBuyStatusClosedEvent closedEvt = groupBuyEventMapper.toGroupBuyClosedEvent(
                        groupBuy.getId(), userId, participantIds,
                        groupBuy.getTitle(), String.valueOf(groupBuy.getParticipantCount()),
                        String.valueOf(groupBuy.getTotalAmount())
                );
                kafkaEventPublisher.publish(
                        GROUPBUY_STATUS_CLOSED,
                        String.valueOf(groupBuy.getId()),
                        objectMapper.writeValueAsString(closedEvt)
                );
            } catch (JsonProcessingException ex) {
                redisTemplate.opsForValue().increment(stockKey, request.getQuantity());
                redisTemplate.delete(orderCheckKey);
                log.error("❌ ClosedEvent serialization failed", ex);
                throw new BusinessException(
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        SERIALIZATION_FAIL
                );
            }
        }

        // 7. Pending 이벤트
        try {
            OrderPendingEvent pendingEvt = orderEventMapper.toPendingEvent(
                    order.getUser().getId(), groupBuy.getId(), userId,
                    order.getUser().getNickname(), order.getQuantity()
            );
            kafkaEventPublisher.publish(
                    ORDER_STATUS_PENDING,
                    String.valueOf(order.getId()),
                    objectMapper.writeValueAsString(pendingEvt)
            );
        } catch (JsonProcessingException e) {
            redisTemplate.opsForValue().increment(stockKey, request.getQuantity());
            redisTemplate.delete(orderCheckKey);
            log.error("❌ PendingEvent serialization failed", e);
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    SERIALIZATION_FAIL
            );
        }

        // 8. 응답 반환
        return OrderCreateResponse.builder()
                .orderId(order.getId())
                .productName(groupBuy.getName())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .hostName(groupBuy.getUser().getName())
                .hostAccountBank(groupBuy.getUser().getAccountBank())
                .hostAccountNumber(groupBuy.getUser().getAccountNumber())
                .build();
    }
}
