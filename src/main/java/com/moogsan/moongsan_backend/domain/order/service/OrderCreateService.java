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
    private final OrderEventMapper eventMapper;
    private final GroupBuyEventMapper groupBuyEventMapper;
    private final ObjectMapper objectMapper;

    // 주문 생성 서비스
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

        GroupBuy groupBuy = groupBuyRepository.findById(request.getPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "공동구매 정보를 찾을 수 없습니다."));

        // 공동구매 글의 상태가 열려있어야지만 주문 가능
        if (!"OPEN".equals(groupBuy.getPostStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "현재 주문이 불가능한 상태입니다.");
        }

        // 동일 postId, userId로 CANCELED, REFUNDED 주문이 3건 이상이면 차단
        int canceledCount = orderRepository.countByUserIdAndGroupBuyIdAndStatusIn(user.getId(), groupBuy.getId(),
                List.of("CANCELED", "REFUNDED"));
        if (canceledCount > 3) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "주문을 3회 이상 취소하였습니다.");
        }

        // 해당 공구 내 CANCELED, REFUNDED 상태가 아닌 주문 존재
        boolean exists = orderRepository.existsByUserIdAndGroupBuyIdAndStatusNotIn(
                user.getId(), groupBuy.getId(), List.of("CANCELED", "REFUNDED"));

        if (exists) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "이미 공동구매에 참여하였습니다.");
        }

        // 입력 수량이 해당 공구의 주문 단위의 배수가 아님
        if (request.getQuantity() % groupBuy.getUnitAmount() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "수량은 주문 단위의 배수여야 합니다.");
        }

        // 입력 수량이 해당 공구의 남은 수량을 초과
        if (request.getQuantity() > groupBuy.getLeftAmount()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "남은 수량을 초과하여 주문할 수 없습니다.");
        }

        String orderName = request.getName() != null ? request.getName() : user.getName();

        Order order = Order.builder()
                .user(user)
                .groupBuy(groupBuy)
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .name(orderName)
                .build();

        groupBuy.decreaseLeftAmount(request.getQuantity());
        groupBuy.increaseParticipantCount();
        groupBuy.updateDueSoonStatus(dueSoonPolicy);

        groupBuyRepository.save(groupBuy);
        orderRepository.save(order);
        chattingCommandFacade.joinChatRoom(user, groupBuy.getId());

        if (groupBuy.getLeftAmount() == 0) {
            groupBuy.changePostStatus("CLOSED");
            try {
                GroupBuyStatusClosedEvent eventDto =
                        groupBuyEventMapper.toGroupBuyClosedEvent(groupBuy, "CLOSED");
                String payload = objectMapper.writeValueAsString(eventDto);
                kafkaEventPublisher.publish(GROUPBUY_STATUS_CLOSED, String.valueOf(groupBuy.getId()), payload);
            } catch (JsonProcessingException e) {
                log.error("❌ Failed to serialize GroupBuyStatusClosedEvent: groupBuyId={}", groupBuy.getId(), e);
                throw new RuntimeException(SERIALIZATION_FAIL, e);
            }
        }
        groupBuyRepository.save(groupBuy);
        groupBuyRepository.flush();

        try {
            OrderPendingEvent eventDto =
                    eventMapper.toPendingEvent(order, groupBuy);
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
