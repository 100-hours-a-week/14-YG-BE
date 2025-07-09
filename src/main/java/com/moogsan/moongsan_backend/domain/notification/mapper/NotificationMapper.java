package com.moogsan.moongsan_backend.domain.notification.mapper;

import com.moogsan.moongsan_backend.domain.notification.dto.NotificationResponse;
import com.moogsan.moongsan_backend.domain.notification.entity.Notification;

public class NotificationMapper {

    public static NotificationResponse toNotificationReponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getNotificationType().toString())
                .createdAt(notification.getCreatedAt())
                .read(notification.getRead())
                .build();
    }
}
