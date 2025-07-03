package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 토픽: groupbuy.pickup.approaching
 * 설명: 공구 픽업일 임박 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBuyPickupApproachingEvent extends BaseEvent{
    private Long groupBuyId;  // 공구 게시글 아이디
}
