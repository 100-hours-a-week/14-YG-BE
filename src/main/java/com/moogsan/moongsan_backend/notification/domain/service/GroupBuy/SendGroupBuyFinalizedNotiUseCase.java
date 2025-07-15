package com.moogsan.moongsan_backend.notification.domain.service.GroupBuy;

import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyStatusFinalizedEvent;
import com.moogsan.moongsan_backend.global.infrastructure.sse.SseEmitterRepository;
import com.moogsan.moongsan_backend.notification.domain.entity.NotificationType;
import com.moogsan.moongsan_backend.notification.application.factory.NotificationFactory;
import com.moogsan.moongsan_backend.notification.domain.repository.NotificationRepository;
import com.moogsan.moongsan_backend.global.infrastructure.kafka.publisher.NotificationPublisher;
import com.moogsan.moongsan_backend.notification.infrastructure.template.NotificationTemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        log.debug("✅ 공구 체결 알림 전송 완료: groupId={}", event.getGroupBuyId());
    }
}
