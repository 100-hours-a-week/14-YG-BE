package com.moogsan.moongsan_backend.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderCanceledEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderConfirmedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderPendingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderRefundedEvent;
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
@Transactional(readOnly = true)
public class SendOrderNotificationUseCase {

    private final SseEmitterRepository emitterRepository;
    private final NotificationTemplateRegistry templateRegistry;
    private final ObjectMapper objectMapper;

    public void handleOrderPending(OrderPendingEvent event) {

        Long hostId = event.getHostId();
        if (hostId == null) {
            log.warn("hostId 가 없음, event={}", event);
            return;
        }

        String title = templateRegistry.title(NotificationType.ORDER_PENDING);
        String body = templateRegistry.body(NotificationType.ORDER_PENDING)
                        .replace("{buyerName}", event.getBuyerName())
                        .replace("{qty}", String.valueOf(event.getQuantity()));

        NotificationPayload payload = new NotificationPayload(title, body, event);

        emitterRepository.send(hostId.toString(),
                NotificationType.ORDER_PENDING.name(),
                payload);

        log.debug("✅ 알림 전송 완료: hostId={}, orderId={}", hostId, event.getOrderId());
    }

    public void handleOrderConfirmed(OrderConfirmedEvent event) {

        Long participantId = event.getParticipantId();
        if (participantId == null) {
            log.warn("participantId 가 없음, event={}", event);
            return;
        }

        String title = templateRegistry.title(NotificationType.ORDER_CONFIRMED);
        String body = templateRegistry.body(NotificationType.ORDER_CONFIRMED)
                .replace("{groupBuyTitle}", event.getGroupBuyName())
                .replace("{buyerName}", event.getBuyerName());

        NotificationPayload payload = new NotificationPayload(title, body, event);

        emitterRepository.send(participantId.toString(),
                NotificationType.ORDER_CONFIRMED.name(),
                payload);

        log.debug("✅ 알림 전송 완료: hostId={}, orderId={}", participantId, event.getOrderId());
    }

    public void handleOrderCanceled(OrderCanceledEvent event) {

        Long hostId = event.getHostId();
        if (hostId == null) {
            log.warn("hostId 가 없음, event={}", event);
            return;
        }

        String title = templateRegistry.title(NotificationType.ORDER_CANCELED);
        String body = templateRegistry.body(NotificationType.ORDER_CANCELED)
                .replace("{buyerName}", event.getBuyerName())
                .replace("{buyerBank}", event.getBuyerBank())
                .replace("{buyerAccount}", event.getBuyerAccount())
                .replace("{price}", String.valueOf(event.getPrice()));

        NotificationPayload payload = new NotificationPayload(title, body, event);

        emitterRepository.send(hostId.toString(),
                NotificationType.ORDER_CANCELED.name(),
                payload);

        log.debug("✅ 알림 전송 완료: hostId={}, orderId={}", hostId, event.getOrderId());
    }

    public void handleOrderRefunded(OrderRefundedEvent event) {

        Long participantId = event.getParticipantId();
        if (participantId == null) {
            log.warn("participantId 가 없음, event={}", event);
            return;
        }

        String title = templateRegistry.title(NotificationType.ORDER_REFUNDED);
        String body = templateRegistry.body(NotificationType.ORDER_REFUNDED)
                .replace("{groupBuyTitle}", event.getGroupBuyName())
                .replace("{buyerName}", event.getBuyerName());

        NotificationPayload payload = new NotificationPayload(title, body, event);

        emitterRepository.send(participantId.toString(),
                NotificationType.ORDER_REFUNDED.name(),
                payload);

        log.debug("✅ 알림 전송 완료: hostId={}, orderId={}", participantId, event.getOrderId());
    }


}
