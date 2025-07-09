package com.moogsan.moongsan_backend.adapters.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyPickupUpdatedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusClosedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusEndedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderPendingEvent;
import com.moogsan.moongsan_backend.domain.notification.service.GroupBuy.SendGroupBuyClosedNotiUseCase;
import com.moogsan.moongsan_backend.domain.notification.service.GroupBuy.SendGroupBuyEndedNotiUseCase;
import com.moogsan.moongsan_backend.domain.notification.service.GroupBuy.SendPickupChangedNotiUseCase;
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
public class GroupBuyNotificationListener {

    private final SendGroupBuyClosedNotiUseCase useCase;
    private final SendGroupBuyEndedNotiUseCase endedNotiUseCase;
    private final SendPickupChangedNotiUseCase pickupChangedNotiUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.GROUPBUY_STATUS_CLOSED,
            groupId = ConsumerGroups.NOTIFICATION
    )
    public void onGroupBuyClosed(GroupBuyStatusClosedEvent event,
                                 Acknowledgment ack) {
        try {

            log.debug("groupBuy.status.closed 수신: {}", event);
            useCase.handleGroupBuyClosed(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ GroupBuyStatusClosedEvent 역직렬화 실패. raw", e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }

    @KafkaListener(
            topics = KafkaTopics.GROUPBUY_STATUS_ENDED,
            groupId = ConsumerGroups.NOTIFICATION
    )
    public void onGroupBuyClosed(GroupBuyStatusEndedEvent event,
                                 Acknowledgment ack) {
        try {

            log.debug("groupBuy.status.ended 수신: {}", event);
            endedNotiUseCase.handleGroupBuyEnded(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ GroupBuyStatusEndedEvent 역직렬화 실패. raw", e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }

    @KafkaListener(
            topics = KafkaTopics.GROUPBUY_PICKUP_UPDATED,
            groupId = ConsumerGroups.NOTIFICATION
    )
    public void onGroupBuyClosed(GroupBuyPickupUpdatedEvent event,
                                 Acknowledgment ack) {
        try {

            log.debug("groupBuy.pickup.updated 수신: {}", event);
            pickupChangedNotiUseCase.handleGroupBuyPickupChanged(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ GroupBuyPickupUpdatedEvent 역직렬화 실패. raw", e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }
}
