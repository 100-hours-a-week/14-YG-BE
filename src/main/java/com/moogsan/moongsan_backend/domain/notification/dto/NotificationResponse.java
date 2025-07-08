package com.moogsan.moongsan_backend.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class NotificationResponse {

    private Long id;
    private String title;
    private String body;
    private String type;
    private LocalDateTime createdAt;
    private boolean read;

}
