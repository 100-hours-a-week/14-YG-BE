package com.moogsan.moongsan_backend.adapters.kafka.consumer.listener;

import com.moogsan.moongsan_backend.adapters.kafka.consumer.ConsumerGroups;
import com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuy.GroupBuyUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyRealtimeListener {

    @KafkaListener(
            topics = KafkaTopics.GROUPBUY_DETAIL_UPDATED,
            groupId = ConsumerGroups.REALTIME_GROUPBUY
    )
    public void onGroupBuyUpdated(GroupBuyUpdatedEvent event,
                                  Acknowledgment ack) {
        try {

            log.debug("groupBuy.detail.updated 수신: {}", event);
            useCase.handleGroupBuyDetailUpdated(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ GroupBuyUpdatedEvent 역직렬화 실패. raw", e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }
}
