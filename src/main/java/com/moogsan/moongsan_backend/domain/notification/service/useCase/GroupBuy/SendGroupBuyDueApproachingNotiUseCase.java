package com.moogsan.moongsan_backend.domain.notification.service.useCase.GroupBuy;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyDueApproachingEvent;
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
public class SendGroupBuyDueApproachingNotiUseCase {

    private final NotificationTemplateRegistry templateRegistry;
    private final NotificationPublisher notificationPublisher;

    public void handleGroupBuyDueApproaching(GroupBuyDueApproachingEvent event) {

        Long hostId = event.getHostId();
        if (hostId == null) {
            log.warn("hostId 가 없음, event={}", event);
            return;
        }

        String title = templateRegistry.title(NotificationType.GROUPBUY_DUE_APPROACHING);
        String hostBody = templateRegistry.body(NotificationType.GROUPBUY_DUE_APPROACHING)
                .replace("{groupBuyTitle}", event.getGroupBuyTitle())
                .replace("{participantCount}", String.valueOf(event.getParticipantCount()))
                .replace("{totalQty}", String.valueOf(event.getLeftQty()))
                .replace("{extraMessage}", "채팅방에 확인 메세지를 보내주세요!");

        String partiBody = templateRegistry.body(NotificationType.GROUPBUY_DUE_APPROACHING)
                .replace("{groupBuyTitle}", event.getGroupBuyTitle())
                .replace("{participantCount}", String.valueOf(event.getParticipantCount()))
                .replace("{totalQty}", String.valueOf(event.getLeftQty()))
                .replace("{extraMessage}", "주최자의 메세지를 확인해주세요!");

        // host에게 알림 발행
        notificationPublisher.publish(
                hostId,
                NotificationType.GROUPBUY_DUE_APPROACHING,
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
                                NotificationType.GROUPBUY_DUE_APPROACHING,
                                title,
                                partiBody,
                                event
                        )
                );

        log.debug("✅ 공구 종료 알림 전송 완료: groupId={}", event.getGroupBuyId());
    }
}
