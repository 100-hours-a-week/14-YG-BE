package com.moogsan.moongsan_backend.domain.user.mapper;

import com.moogsan.moongsan_backend.domain.user.event.UserProfileUpdatedEvent;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserEventMapper {

    // 주문 생성 이벤트
    public UserProfileUpdatedEvent toPendingEvent(User user) {
        return UserProfileUpdatedEvent.builder()
                .userId(user.getId())
                .occurredAt(Instant.now().toString())
                .build();
    }
}
