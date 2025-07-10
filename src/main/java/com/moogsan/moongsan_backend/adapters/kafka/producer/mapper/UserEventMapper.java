package com.moogsan.moongsan_backend.adapters.kafka.producer.mapper;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.UserProfileUpdatedEvent;
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
