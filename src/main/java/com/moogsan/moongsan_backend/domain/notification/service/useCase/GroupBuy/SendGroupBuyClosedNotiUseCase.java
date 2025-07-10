package com.moogsan.moongsan_backend.domain.notification.service.useCase.GroupBuy;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusClosedEvent;
import com.moogsan.moongsan_backend.domain.notification.entity.NotificationType;
import com.moogsan.moongsan_backend.domain.notification.service.publisher.NotificationPublisher;
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
        String body = templateRegistry.body(NotificationType.GROUPBUY_STATUS_CLOSED)
                .replace("{groupBuyTitle}", event.getGroupBuyTitle())
                .replace("{participantCount}", String.valueOf(event.getParticipantCount()))
                .replace("{totalQty}", String.valueOf(event.getTotalQty()));

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
