package com.moogsan.moongsan_backend.domain.notification.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.notification.entity.Notification;
import com.moogsan.moongsan_backend.domain.notification.entity.NotificationType;
import com.moogsan.moongsan_backend.domain.notification.template.NotificationTemplateRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationFactory {

    private final NotificationTemplateRegistry registry;
    private final ObjectMapper objectMapper;

    public Notification create(
            NotificationType type, Long receiverId, Object event, Map<String, String> variables
    ) {
        String title = registry.title(type);
        String body = registry.body(type);

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            body = body.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        Map<String, Object> data = objectMapper.convertValue(event, Map.class);

        return Notification.builder()
                .receiverId(receiverId)
                .title(title)
                .body(body)
                .notificationType(type)
                .data(data)
                .createdAt(LocalDateTime.now())
                .build();

    }
}
