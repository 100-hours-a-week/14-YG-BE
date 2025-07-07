package com.moogsan.moongsan_backend.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderPendingEvent;
import com.moogsan.moongsan_backend.adapters.sse.SseEmitterRepository;
import com.moogsan.moongsan_backend.domain.notification.entity.NotificationType;
import com.moogsan.moongsan_backend.domain.notification.template.NotificationPayload;
import com.moogsan.moongsan_backend.domain.notification.template.NotificationTemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendOrderNotificationUseCase {

    private final SseEmitterRepository emitterRepository;
    private final NotificationTemplateRegistry templateRegistry;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public void handleOrderPending(OrderPendingEvent event) {

        Long hostId = event.getHostId();
        if (hostId == null) {
            log.warn("hostId 가 없음, event={}", event);
            return;
        }

        String title = templateRegistry.title(NotificationType.ORDER_PENDING);
        String body = templateRegistry.title(NotificationType.ORDER_PENDING)
                        .replace("{buyerName}", event.getBuyerName())
                        .replace("{qty}", String.valueOf(event.getQuantity()));

        NotificationPayload payload = new NotificationPayload(title, body, event);

        emitterRepository.send(hostId.toString(),
                NotificationType.ORDER_PENDING.name(),
                payload);

        log.debug("✅ 알림 전송 완료: hostId={}, orderId={}", hostId, event.getOrderId());
    }


}
