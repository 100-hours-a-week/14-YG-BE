package com.moogsan.moongsan_backend.adapters.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderConfirmedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderPendingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderRefundedEvent;
import com.moogsan.moongsan_backend.domain.notification.service.SendOrderNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final SendOrderNotificationUseCase useCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.ORDER_STATUS_PENDING,
            groupId = ConsumerGroups.NOTIFICATION
    )
    public void onOrderPending(OrderPendingEvent event,
                               Acknowledgment ack) {
        try {

            log.debug("order.status.pending 수신: {}", event);
            useCase.handleOrderPending(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ OrderPendingEvent 역직렬화 실패. raw", e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }

    @KafkaListener(
            topics = KafkaTopics.ORDER_STATUS_CONFIRMED,
            groupId = ConsumerGroups.NOTIFICATION
    )
    public void onOrderConfirmed(OrderConfirmedEvent event,
                                 Acknowledgment ack) {
        try {

            log.debug("order.status.confirmed 수신: {}", event);
            useCase.handleOrderConfirmed(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ OrderConfirmedEvent 역직렬화 실패. raw", e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }

    @KafkaListener(
            topics = KafkaTopics.ORDER_STATUS_CANCELED,
            groupId = ConsumerGroups.NOTIFICATION
    )
    public void onOrderCanceled(OrderCanceledEvent event,
                                Acknowledgment ack) {
        try {

            log.debug("order.status.canceled 수신: {}", event);
            useCase.handleOrderCanceled(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ OrderCanceledEvent 역직렬화 실패. raw", e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }



    @KafkaListener(
            topics = KafkaTopics.ORDER_STATUS_REFUNDED,
            groupId = ConsumerGroups.NOTIFICATION
    )
    public void onOrderRefunded(OrderRefundedEvent event,
                                 Acknowledgment ack) {
        try {

            log.debug("order.status.refunded 수신: {}", event);
            useCase.handleOrderRefunded(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ OrderRefundedEvent 역직렬화 실패. raw", e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }
}
