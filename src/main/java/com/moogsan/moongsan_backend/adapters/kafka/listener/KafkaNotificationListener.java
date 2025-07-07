package com.moogsan.moongsan_backend.adapters.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderPendingEvent;
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
public class KafkaNotificationListener {

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
}
