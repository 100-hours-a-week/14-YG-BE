package com.moogsan.moongsan_backend.domain.notification.service.useCase.GroupBuy;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyPickupUpdatedEvent;
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
public class SendPickupChangedNotiUseCase {

    private final NotificationTemplateRegistry templateRegistry;
    private final NotificationPublisher notificationPublisher;

    public void handleGroupBuyPickupChanged(GroupBuyPickupUpdatedEvent event) {

        String title = templateRegistry.title(NotificationType.GROUPBUY_PICKUP_UPDATED);

        String partiBody = templateRegistry.body(NotificationType.GROUPBUY_PICKUP_UPDATED)
                .replace("{pickupDate}", event.getPickupDate())
                .replace("{dateModificationReason}", event.getDateModificationReason());


        // 참가자들에게 알림 발행 (host 제외)
        event.getParticipantIds()
                .forEach(participantId ->
                        notificationPublisher.publish(
                                participantId,
                                NotificationType.GROUPBUY_PICKUP_UPDATED,
                                title,
                                partiBody,
                                event
                        )
                );

        log.debug("✅ 공구 픽업 변경 알림 전송 완료: groupId={}", event.getGroupBuyId());
    }
}
