package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토픽: user.profile.updated
 * 설명: 유저 프로필 수정 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdatedEvent extends BaseEvent{
    private Long userId;  // 주문 아이디
}
