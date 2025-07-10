package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 토픽: user.profile.updated
 * 설명: 유저 프로필 수정 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserProfileUpdatedEvent extends BaseEvent{
    private Long userId;  // 주문 아이디
}
