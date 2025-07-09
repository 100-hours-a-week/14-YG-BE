package com.moogsan.moongsan_backend.domain.notification.service.GroupBuy;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyPickupUpdatedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusClosedEvent;
import com.moogsan.moongsan_backend.adapters.sse.SseEmitterRepository;
import com.moogsan.moongsan_backend.domain.notification.entity.Notification;
import com.moogsan.moongsan_backend.domain.notification.entity.NotificationType;
import com.moogsan.moongsan_backend.domain.notification.factory.NotificationFactory;
import com.moogsan.moongsan_backend.domain.notification.repository.NotificationRepository;
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
public class SendPickupChangedNotiUseCase {
    private final SseEmitterRepository emitterRepository;
    private final NotificationTemplateRegistry templateRegistry;
    private final NotificationFactory notificationFactory;
    private final NotificationRepository notificationRepository;

    public void handleGroupBuyPickupChanged(GroupBuyPickupUpdatedEvent event) {

        String title = templateRegistry.title(NotificationType.GROUPBUY_PICKUP_UPDATED);
        String body = templateRegistry.body(NotificationType.GROUPBUY_PICKUP_UPDATED);

        NotificationPayload payload = new NotificationPayload(title, body, event);

        List<Notification> participantNotis = event.getParticipantIds().stream()
                .map(id -> notificationFactory.create(
                        NotificationType.GROUPBUY_PICKUP_UPDATED,
                        id,
                        event,
                        Map.of(
                                "pickupDate", event.getPickupDate(),
                                "dateModificationReason", event.getDateModificationReason()
                        )
                ))
                .toList();

        notificationRepository.saveAll(participantNotis);

        participantNotis.forEach(noti ->
                emitterRepository.send(noti.getReceiverId().toString(),
                        NotificationType.GROUPBUY_PICKUP_UPDATED.name(), payload));

        log.debug("✅ 알림 전송 완료: groupId={}", event.getGroupBuyId());
    }
}
