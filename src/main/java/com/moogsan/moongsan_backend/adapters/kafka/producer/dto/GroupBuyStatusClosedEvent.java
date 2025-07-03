package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토픽: groupbuy.status.closed
 * 설명: 공구 모집 마감 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBuyStatusClosedEvent extends BaseEvent{
    private Long groupBuyId;  // 공구 게시글 아이디
}
