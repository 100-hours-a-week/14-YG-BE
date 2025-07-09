package com.moogsan.moongsan_backend.adapters.kafka.producer.mapper;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.*;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class GroupBuyEventMapper {

    // 공동구매 모집 종료 이벤트
    public GroupBuyStatusClosedEvent toGroupBuyClosedEvent(
            Long groupBuyId, Long hostId, List<Long> participantIds, String groupBuyTitle,
            String participantCount, String totalQty
    ) {
        return GroupBuyStatusClosedEvent.builder()
                .groupBuyId(groupBuyId)
                .hostId(hostId)
                .participantIds(participantIds)
                .groupBuyTitle(groupBuyTitle)
                .participantCount(participantCount)
                .totalQty(totalQty)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 공동구매 마감 이벤트
    public GroupBuyStatusEndedEvent toGroupBuyEndedEvent(
            Long groupBuyId, Long hostId, List<Long> participantIds, String groupBuyTitle,
            String participantCount, String totalQty
    ) {
        return GroupBuyStatusEndedEvent.builder()
                .groupBuyId(groupBuyId)
                .hostId(hostId)
                .participantIds(participantIds)
                .groupBuyTitle(groupBuyTitle)
                .participantCount(participantCount)
                .totalQty(totalQty)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 공동구매 마감일자 임박 이벤트
    public GroupBuyDueApproachingEvent toGroupBuyDueApproachingEvent(
            Long groupBuyId, Long hostId, String groupBuyTitle,
            List<Long> participantIds, String participantCount, String leftQty
    ) {
        return GroupBuyDueApproachingEvent.builder()
                .groupBuyId(groupBuyId)
                .hostId(hostId)
                .participantIds(participantIds)
                .groupBuyTitle(groupBuyTitle)
                .participantCount(participantCount)
                .leftQty(leftQty)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 공동구매 픽업일자 임박 이벤트
    public GroupBuyPickupApproachingEvent toGroupBuyPickupApproachingEvent(
            Long groupBuyId, Long hostId, String groupBuyTitle,
            List<Long> participantIds, String participantCount, String totalQty
    ) {
        return GroupBuyPickupApproachingEvent.builder()
                .groupBuyId(groupBuyId)
                .hostId(hostId)
                .participantIds(participantIds)
                .groupBuyTitle(groupBuyTitle)
                .participantCount(participantCount)
                .totalQty(totalQty)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 공동구매 픽업일자 변경 이벤트
    public GroupBuyPickupUpdatedEvent toGroupBuyPickupUpdatedEvent(
            Long groupBuyId, List<Long> participantIds, String groupBuyTitle,
            String pickupDate, String dateModificationReason
    ) {
        return GroupBuyPickupUpdatedEvent.builder()
                .groupBuyId(groupBuyId)
                .participantIds(participantIds)
                .groupBuyTitle(groupBuyTitle)
                .pickupDate(pickupDate)
                .dateModificationReason(dateModificationReason)
                .occurredAt(Instant.now().toString())
                .build();
    }
}
