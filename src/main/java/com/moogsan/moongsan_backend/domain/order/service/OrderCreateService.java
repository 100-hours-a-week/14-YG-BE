package com.moogsan.moongsan_backend.domain.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyPickupUpdatedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusClosedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusEndedEvent;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.redisson.api.RedissonClient;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;
import java.util.List;

import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.*;
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

    // 주문 생성 서비스
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long userId) {
        // 유저, 공동구매 정보 조회 - 404 반환
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

        GroupBuy groupBuy = groupBuyRepository.findById(request.getPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "공동구매 정보를 찾을 수 없습니다."));

        // 공동구매 게시 상태 조회 - 400 반환
        if (!"OPEN".equals(groupBuy.getPostStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "현재 주문이 불가능한 상태입니다.");
        }

        // 동일 공동구매 주문 횟수 제한 - 409 반환
        int canceledCount = orderRepository.countByUserIdAndGroupBuyIdAndStatusIn(user.getId(), groupBuy.getId(),
                List.of("CANCELED", "REFUNDED"));
        if (canceledCount > 3) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "주문을 3회 이상 취소하였습니다.");
        }

        // 환불중인 주문 존재 - 409 반환
        boolean existsCanceled = orderRepository.existsByUserIdAndGroupBuyIdAndStatusIn(
                user.getId(), groupBuy.getId(), List.of("CANCELED"));

        if (existsCanceled) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "환불중인 주문이 존재합니다.");
        }

        // 참여중인 주문 존재 - 409 반환
        boolean exists = orderRepository.existsByUserIdAndGroupBuyIdAndStatusNotIn(
                user.getId(), groupBuy.getId(), List.of("CANCELED", "REFUNDED"));

        if (exists) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "이미 공동구매에 참여하였습니다.");
        }

        // 주문 수량이 해당 공동구매 주문 단위의 배수가 아님
        if (request.getQuantity() % groupBuy.getUnitAmount() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "수량은 주문 단위의 배수여야 합니다.");
        }

        String orderName = request.getName() != null ? request.getName() : user.getName();

        Order order = Order.builder()
                .user(user)
                .groupBuy(groupBuy)
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .name(orderName)
                .build();

        // 1. Redis 키 정의
        String stockKey = "order:groupbuy:stock:" + request.getPostId();
        String orderCheckKey = "order:user:" + userId + ":groupbuy:" + request.getPostId();
        String lockKey = "order:lock:groupbuy:" + request.getPostId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean locked = lock.tryLock(3, 2, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "잠시 후 다시 시도해주세요.");
            }

            // 2. Redis 중복 주문 방지
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(orderCheckKey, "1", Duration.ofMinutes(10));
            if (Boolean.FALSE.equals(isNew)) {
                throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "이미 공동구매에 참여하였습니다.");
            }

            // 3. Lua Script로 재고 감소
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(
                    "local stock = redis.call('get', KEYS[1]);" +
                            "if (stock and tonumber(stock) >= tonumber(ARGV[1])) then " +
                            " return redis.call('decrby', KEYS[1], ARGV[1]); " +
                            "else return -1; end"
            );
            redisScript.setResultType(Long.class);

            Long redisResult = redisTemplate.execute(
                    redisScript,
                    List.of(stockKey),
                    String.valueOf(request.getQuantity())
            );

            if (redisResult == null || redisResult < 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "남은 수량을 초과하여 주문할 수 없습니다.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "락 획득 중 오류가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }


        groupBuy.decreaseLeftAmount(request.getQuantity());
        groupBuy.increaseParticipantCount();
        groupBuy.updateDueSoonStatus(dueSoonPolicy);

        orderRepository.save(order);
        chattingCommandFacade.joinChatRoom(user, groupBuy.getId());

        if (groupBuy.getLeftAmount() == 0) {
            groupBuy.changePostStatus("CLOSED");

            List<Order> orders = orderRepository.findAllByGroupBuyIdOrderByStatusCustom(groupBuy.getId());

            List<Long> participantIds = orders.stream()
                    .map(o -> o.getUser().getId())
                    .distinct()
                    .toList();

            try {
                GroupBuyStatusClosedEvent eventDto =
                        groupBuyEventMapper.toGroupBuyClosedEvent(
                                groupBuy.getId(),
                                groupBuy.getUser().getId(),
                                participantIds,
                                groupBuy.getTitle(),
                                String.valueOf(groupBuy.getParticipantCount()),
                                String.valueOf(groupBuy.getTotalAmount())
                        );
                String payload = objectMapper.writeValueAsString(eventDto);
                kafkaEventPublisher.publish(GROUPBUY_STATUS_CLOSED, String.valueOf(groupBuy.getId()), payload);
            } catch (JsonProcessingException e) {
                log.error("❌ Failed to serialize GroupBuyStatusClosedEvent: groupBuyId={}", groupBuy.getId(), e);
                throw new RuntimeException(SERIALIZATION_FAIL, e);
            } catch (Exception e) {
                // Redis 롤백 처리
                redisTemplate.opsForValue().increment(stockKey, request.getQuantity());
                redisTemplate.delete(orderCheckKey);
                throw e;
            }

            groupBuyRepository.save(groupBuy);
            groupBuyRepository.flush();

        }

        try {
            OrderPendingEvent eventDto =
                    orderEventMapper.toPendingEvent(
                            order.getId(),
                            groupBuy.getId(),
                            groupBuy.getUser().getId(),
                            order.getUser().getNickname(),
                            order.getQuantity()
                    );
            log.info("▶ orderPendingEvent DTO = {}", eventDto);
            String payload = objectMapper.writeValueAsString(eventDto);
            kafkaEventPublisher.publish(ORDER_STATUS_PENDING, String.valueOf(order.getId()), payload);
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize OrderPendingEvent: orderId={}", order.getId(), e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }

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
