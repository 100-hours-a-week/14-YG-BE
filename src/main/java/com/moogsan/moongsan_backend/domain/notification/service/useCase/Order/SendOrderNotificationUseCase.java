package com.moogsan.moongsan_backend.domain.notification.service.useCase.Order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderCanceledEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderConfirmedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderPendingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderRefundedEvent;
import com.moogsan.moongsan_backend.adapters.sse.SseEmitterRepository;
import com.moogsan.moongsan_backend.domain.notification.entity.NotificationType;
import com.moogsan.moongsan_backend.domain.notification.factory.NotificationFactory;
import com.moogsan.moongsan_backend.domain.notification.repository.NotificationRepository;
import com.moogsan.moongsan_backend.domain.notification.service.publisher.NotificationPublisher;
import com.moogsan.moongsan_backend.domain.notification.template.NotificationTemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SendOrderNotificationUseCase {

    private final SseEmitterRepository emitterRepository;
    private final NotificationTemplateRegistry templateRegistry;
    private final NotificationFactory notificationFactory;
    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;
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

        notificationPublisher.publish(
                hostId,
                NotificationType.ORDER_PENDING,
                title,
                body,
                event
        );

        log.debug("✅ 주문 생성 알림 전송 완료: hostId={}, orderId={}", hostId, event.getOrderId());
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

        notificationPublisher.publish(
                participantId,
                NotificationType.ORDER_CONFIRMED,
                title,
                body,
                event
        );

        log.debug("✅ 주문 확인 알림 전송 완료: hostId={}, orderId={}", participantId, event.getOrderId());
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

        notificationPublisher.publish(
                hostId,
                NotificationType.ORDER_CANCELED,
                title,
                body,
                event
        );

        log.debug("✅ 주문 취소 알림 전송 완료: hostId={}, orderId={}", hostId, event.getOrderId());
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

        notificationPublisher.publish(
                participantId,
                NotificationType.ORDER_REFUNDED,
                title,
                body,
                event
        );

        log.debug("✅ 주문 환불 알림 전송 완료: hostId={}, orderId={}", participantId, event.getOrderId());
    }


}
