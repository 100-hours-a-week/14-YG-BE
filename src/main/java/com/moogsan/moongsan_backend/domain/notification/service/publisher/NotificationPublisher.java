package com.moogsan.moongsan_backend.domain.notification.service.publisher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.sse.SseEmitterRepository;
import com.moogsan.moongsan_backend.domain.notification.dto.NotificationResponse;
import com.moogsan.moongsan_backend.domain.notification.entity.Notification;
import com.moogsan.moongsan_backend.domain.notification.entity.NotificationType;
import com.moogsan.moongsan_backend.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {
    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository emitterRepository;
    private final ObjectMapper objectMapper;

    public void publish(Long userId,
                        NotificationType type,
                        String title,
                        String body,
                        Object rawPayload) {
        try {
            // rawPayload → Map 변환
            Map<String, Object> dataMap = objectMapper.convertValue(
                    rawPayload,
                    new TypeReference<Map<String, Object>>() {}
            );

            // 1) 엔티티 빌드 & 저장
            Notification entity = Notification.builder()
                    .receiverId(userId)
                    .notificationType(type)
                    .title(title)
                    .body(body)
                    .data(dataMap)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(entity);

            // 2) DTO 변환
            NotificationResponse dto = NotificationResponse.builder()
                    .id(entity.getId())
                    .type(entity.getNotificationType().name())
                    .title(entity.getTitle())
                    .body(entity.getBody())
                    .payload(entity.getData())
                    .createdAt(entity.getCreatedAt())
                    .read(entity.getRead())
                    .build();

            // 3) SSE 전송
            emitterRepository.send(
                    userId.toString(),
                    entity.getNotificationType().name(),
                    dto
            );
        } catch (Exception e) {
            throw new RuntimeException("Notification publish failed", e);
        }
    }
}
