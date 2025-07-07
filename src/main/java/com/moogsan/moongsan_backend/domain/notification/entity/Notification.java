package com.moogsan.moongsan_backend.domain.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class Notification {
    private final Long receiverId;
    private final String title;
    private String body;
    private NotificationType notificationType;
    private Map<String, Object> data;
}
