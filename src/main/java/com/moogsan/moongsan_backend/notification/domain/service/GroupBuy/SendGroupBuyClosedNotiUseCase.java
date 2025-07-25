package com.moogsan.moongsan_backend.notification.domain.service.GroupBuy;

import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyStatusClosedEvent;
import com.moogsan.moongsan_backend.notification.domain.entity.NotificationType;
import com.moogsan.moongsan_backend.notification.infrastructure.publisher.NotificationPublisher;
import com.moogsan.moongsan_backend.notification.infrastructure.template.NotificationTemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SendGroupBuyClosedNotiUseCase {

    private final NotificationTemplateRegistry templateRegistry;
    private final NotificationPublisher notificationPublisher;

    public void handleGroupBuyClosed(GroupBuyStatusClosedEvent event) {

        Long hostId = event.getHostId();
        if (hostId == null) {
            log.warn("hostId 가 없음, event={}", event);
            return;
        }

        String title = templateRegistry.title(NotificationType.GROUPBUY_STATUS_CLOSED);
        String body = templateRegistry.body(NotificationType.GROUPBUY_STATUS_CLOSED);

        // host에게 알림 발행
        notificationPublisher.publish(
                hostId,
                NotificationType.GROUPBUY_STATUS_CLOSED,
                title,
                body,
                event
        );

        // 참가자들에게 알림 발행 (host 제외)
        event.getParticipantIds().stream()
                .filter(id -> !id.equals(hostId))
                .forEach(participantId ->
                        notificationPublisher.publish(
                                participantId,
                                NotificationType.GROUPBUY_STATUS_CLOSED,
                                title,
                                body,
                                event
                        )
                );

        log.debug("✅ 알림 전송 완료: groupId={}", event.getGroupBuyId());
    }
}
