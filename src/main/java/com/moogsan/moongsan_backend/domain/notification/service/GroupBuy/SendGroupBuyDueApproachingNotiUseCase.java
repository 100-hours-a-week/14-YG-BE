package com.moogsan.moongsan_backend.domain.notification.service.GroupBuy;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyDueApproachingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusEndedEvent;
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
public class SendGroupBuyDueApproachingNotiUseCase {

    private final SseEmitterRepository emitterRepository;
    private final NotificationTemplateRegistry templateRegistry;
    private final NotificationFactory notificationFactory;
    private final NotificationRepository notificationRepository;

    public void handleGroupBuyDueApproaching(GroupBuyDueApproachingEvent event) {

        Long hostId = event.getHostId();
        if (hostId == null) {
            log.warn("hostId 가 없음, event={}", event);
            return;
        }

        String title = templateRegistry.title(NotificationType.GROUPBUY_DUE_APPROACHING);
        String body = templateRegistry.body(NotificationType.GROUPBUY_DUE_APPROACHING);

        NotificationPayload payload = new NotificationPayload(title, body, event);

        Notification hostNoti = notificationFactory.create(
                NotificationType.GROUPBUY_DUE_APPROACHING,
                hostId,
                event,
                Map.of(
                        "groupBuyTitle", event.getGroupBuyTitle(),
                        "participantCount", event.getParticipantCount(),
                        "totalQty", String.valueOf(event.getLeftQty()),
                        "extraMessage","채팅방에 확인 메세지를 보내주세요!"
                )
        );

        List<Notification> participantNotis = event.getParticipantIds().stream()
                .filter(id -> !id.equals(hostId))
                .map(id -> notificationFactory.create(
                        NotificationType.GROUPBUY_DUE_APPROACHING,
                        id,
                        event,
                        Map.of(
                                "groupBuyTitle", event.getGroupBuyTitle(),
                                "participantCount", event.getParticipantCount(),
                                "totalQty", String.valueOf(event.getLeftQty()),
                                "extraMessage", "주최자의 메세지를 확인해주세요!"
                        )
                ))
                .toList();

        notificationRepository.saveAll(Stream.concat(
                Stream.of(hostNoti),
                participantNotis.stream()
        ).toList());

        emitterRepository.send(hostId.toString(),
                NotificationType.GROUPBUY_DUE_APPROACHING.name(),
                payload);

        participantNotis.forEach(noti ->
                emitterRepository.send(noti.getReceiverId().toString(),
                        NotificationType.GROUPBUY_DUE_APPROACHING.name(), payload));

        log.debug("✅ 공구 종료 알림 전송 완료: groupId={}", event.getGroupBuyId());
    }
}
