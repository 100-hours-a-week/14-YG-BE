package com.moogsan.moongsan_backend.adapters.kafka.producer.mapper;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.*;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class GroupBuyEventMapper {

    // 공동구매 모집 종료 이벤트
    public GroupBuyStatusClosedEvent toGroupBuyClosedEvent(GroupBuy groupBuy, String newStatus) {
        return GroupBuyStatusClosedEvent.builder()
                .groupBuyId(groupBuy.getId())
                .newStatus(newStatus)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 공동구매 마감 이벤트
    public GroupBuyStatusEndedEvent toGroupBuyEndedEvent(GroupBuy groupBuy, String newStatus) {
        return GroupBuyStatusEndedEvent.builder()
                .groupBuyId(groupBuy.getId())
                .newStatus(newStatus)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 공동구매 마감일자 임박 이벤트
    public GroupBuyDueApproachingEvent toGroupBuyDueApproachingEvent(GroupBuy groupBuy) {
        return GroupBuyDueApproachingEvent.builder()
                .groupBuyId(groupBuy.getId())
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 공동구매 픽업일자 임박 이벤트
    public GroupBuyPickupApproachingEvent toGroupBuyPickupApproachingEvent(GroupBuy groupBuy) {
        return GroupBuyPickupApproachingEvent.builder()
                .groupBuyId(groupBuy.getId())
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 공동구매 픽업일자 변경 이벤트
    public GroupBuyPickupUpdatedEvent toGroupBuyPickupUpdatedEvent(GroupBuy groupBuy) {
        return GroupBuyPickupUpdatedEvent.builder()
                .groupBuyId(groupBuy.getId())
                .occurredAt(Instant.now().toString())
                .build();
    }
}
