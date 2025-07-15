package com.moogsan.moongsan_backend.groupbuy.domain.event;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 토픽: groupbuy.detail.updated
 * 설명: 공구 체결 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GroupBuyUpdatedEvent extends BaseEvent {
    private Long groupBuyId;  // 공구 게시글 아이디
}
