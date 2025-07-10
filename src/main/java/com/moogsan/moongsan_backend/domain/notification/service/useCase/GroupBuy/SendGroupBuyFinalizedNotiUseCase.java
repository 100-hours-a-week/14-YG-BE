package com.moogsan.moongsan_backend.domain.notification.service.useCase.GroupBuy;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusClosedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusFinalizedEvent;
import com.moogsan.moongsan_backend.adapters.sse.SseEmitterRepository;
import com.moogsan.moongsan_backend.domain.notification.entity.Notification;
import com.moogsan.moongsan_backend.domain.notification.entity.NotificationType;
import com.moogsan.moongsan_backend.domain.notification.factory.NotificationFactory;
import com.moogsan.moongsan_backend.domain.notification.repository.NotificationRepository;
import com.moogsan.moongsan_backend.domain.notification.service.publisher.NotificationPublisher;
import com.moogsan.moongsan_backend.domain.notification.template.NotificationPayload;
import com.moogsan.moongsan_backend.domain.notification.template.NotificationTemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SendGroupBuyFinalizedNotiUseCase {
    private final SseEmitterRepository emitterRepository;
    private final NotificationTemplateRegistry templateRegistry;
    private final NotificationFactory notificationFactory;
    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;

    public void handleGroupBuyFinalized(GroupBuyStatusFinalizedEvent event) {

        Long hostId = event.getHostId();
        if (hostId == null) {
            log.warn("hostId 가 없음, event={}", event);
            return;
        }

        String title = templateRegistry.title(NotificationType.GROUPBUY_STATUS_FINALIZED);
        String hostBody = templateRegistry.body(NotificationType.GROUPBUY_STATUS_FINALIZED)
                .replace("{groupBuyTitle}", event.getGroupBuyTitle())
                .replace("{participantCount}", String.valueOf(event.getParticipantCount()))
                .replace("{totalQty}", String.valueOf(event.getTotalQty()));

        String partiBody = templateRegistry.body(NotificationType.GROUPBUY_STATUS_FINALIZED)
                .replace("{groupBuyTitle}", event.getGroupBuyTitle())
                .replace("{participantCount}", String.valueOf(event.getParticipantCount()))
                .replace("{totalQty}", String.valueOf(event.getTotalQty()));

        // host에게 알림 발행
        notificationPublisher.publish(
                hostId,
                NotificationType.GROUPBUY_STATUS_FINALIZED,
                title,
                hostBody,
                event
        );

        // 참가자들에게 알림 발행 (host 제외)
        event.getParticipantIds().stream()
                .filter(id -> !id.equals(hostId))
                .forEach(participantId ->
                        notificationPublisher.publish(
                                participantId,
                                NotificationType.GROUPBUY_STATUS_FINALIZED,
                                title,
                                partiBody,
                                event
                        )
                );

        log.debug("✅ 알림 전송 완료: groupId={}", event.getGroupBuyId());
    }
}
