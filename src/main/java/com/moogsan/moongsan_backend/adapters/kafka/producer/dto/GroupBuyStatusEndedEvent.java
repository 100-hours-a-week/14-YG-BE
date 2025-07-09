package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 토픽: groupbuy.status.ended
 * 설명: 공구 종료 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GroupBuyStatusEndedEvent extends BaseEvent{
    private Long groupBuyId;  // 공구 게시글 아이디
    private String newStatus; // 공구 게시글 상태
}
