package com.moogsan.moongsan_backend.domain.notification.service.useCase.GroupBuy;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusEndedEvent;
import com.moogsan.moongsan_backend.domain.notification.entity.NotificationType;
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
public class SendGroupBuyEndedNotiUseCase {

    private final NotificationTemplateRegistry templateRegistry;
    private final NotificationPublisher notificationPublisher;

    public void handleGroupBuyEnded(GroupBuyStatusEndedEvent event) {

        Long hostId = event.getHostId();
        if (hostId == null) {
            log.warn("hostId 가 없음, event={}", event);
            return;
        }

        String title = templateRegistry.title(NotificationType.GROUPBUY_STATUS_ENDED);
        String hostBody = templateRegistry.body(NotificationType.GROUPBUY_STATUS_ENDED)
                .replace("{groupBuyTitle}", event.getGroupBuyTitle())
                .replace("{extraMessage}", "다음 공구에서 만나요!");

        String partiBody = templateRegistry.body(NotificationType.GROUPBUY_STATUS_ENDED)
                .replace("{groupBuyTitle}", event.getGroupBuyTitle())
                .replace("{extraMessage}", "참여해 주셔서 감사합니다! 🎉");

        // host에게 알림 발행
        notificationPublisher.publish(
                hostId,
                NotificationType.GROUPBUY_STATUS_ENDED,
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
                                NotificationType.GROUPBUY_STATUS_ENDED,
                                title,
                                partiBody,
                                event
                        )
                );

        log.debug("✅ 공구 종료 알림 전송 완료: groupId={}", event.getGroupBuyId());
    }
}
