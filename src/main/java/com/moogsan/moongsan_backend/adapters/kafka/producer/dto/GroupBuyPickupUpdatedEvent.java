package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 토픽: groupbuy.pickup.updated
 * 설명: 공구 픽업일 변경 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GroupBuyPickupUpdatedEvent extends BaseEvent{
    private Long groupBuyId;                // 공구 게시글 아이디
    private List<Long> participantIds;      // 공구 참여자 아이디 리스트
    private String groupBuyTitle;           // 공구 게시글 제목
    private String pickupDate;              // 새로운 공구 픽업일자
    private String dateModificationReason;  // 공구 픽업일자 변경 사유
}
