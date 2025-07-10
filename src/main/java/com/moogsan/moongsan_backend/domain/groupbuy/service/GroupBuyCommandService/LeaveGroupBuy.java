package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderCanceledEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderPendingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.OrderEventMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.domain.chatting.participant.Facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.policy.DueSoonPolicy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.exception.specific.OrderNotFoundException;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.ORDER_STATUS_CANCELED;
import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.ORDER_STATUS_PENDING;
import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_OPEN;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LeaveGroupBuy {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final DueSoonPolicy dueSoonPolicy;
    private final ChattingCommandFacade chattingCommandFacade;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final OrderEventMapper eventMapper;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    /// 공구 참여 취소
    public void leaveGroupBuy(User currentUser, Long postId) {

        // 해당 공구가 존재하는지 조회 -> 없으면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구가 OPEN인지 조회, dueDate가 현재 이후인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
                || groupBuy.getDueDate().isBefore(LocalDateTime.now(clock))) {
            throw new GroupBuyInvalidStateException(NOT_OPEN);
        }

        // 해당 공구의 주문 테이블에 해당 유저의 주문이 존재하는지 조회 -> 아니면 404
        Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(currentUser.getId(), groupBuy.getId(),
                        List.of("CANCELED", "REFUNDED"))
                .orElseThrow(OrderNotFoundException::new);

        // 해당 주문의 상태가 paid인지 조회
        //if (order.getStatus().equals("PAID")) {
        //    // 별도의 환불 로직 처리 필요
        //}

        // 참여자 채팅방 나가기
        chattingCommandFacade.leaveChatRoom(currentUser, postId);

        // 남은 수량, 참여 인원 수 업데이트
        int returnQuantity = order.getQuantity();
        groupBuy.increaseLeftAmount(returnQuantity);
        groupBuy.decreaseParticipantCount();

        // 해당 유저의 주문을 취소
        order.setStatus("CANCELED");

        groupBuy.updateDueSoonStatus(dueSoonPolicy);

        orderRepository.save(order);

        int price = order.getPrice();
        int quantity = order.getQuantity();
        int totalPrice = price * quantity;
        try {
            OrderCanceledEvent eventDto =
                    eventDto = eventMapper.toCanceledEvent(
                            order.getId(),
                            groupBuy.getId(),
                            groupBuy.getUser().getId(),
                            order.getUser().getNickname(),
                            order.getUser().getAccountBank(),
                            order.getUser().getAccountNumber(),
                            totalPrice
                    );
            log.info("▶ orderCanceledEvent DTO = {}", eventDto);
            String payload = objectMapper.writeValueAsString(eventDto);
            kafkaEventPublisher.publish(ORDER_STATUS_CANCELED, String.valueOf(order.getId()), payload);
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize OrderCanceledEvent: orderId={}", order.getId(), e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }

    }
}
