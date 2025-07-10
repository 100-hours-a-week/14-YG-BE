package com.moogsan.moongsan_backend.domain.notification.service.GroupBuy;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusClosedEvent;
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
public class SendGroupBuyEndedNotiUseCase {

    private final SseEmitterRepository emitterRepository;
    private final NotificationTemplateRegistry templateRegistry;
    private final NotificationFactory notificationFactory;
    private final NotificationRepository notificationRepository;

    public void handleGroupBuyEnded(GroupBuyStatusEndedEvent event) {

        Long hostId = event.getHostId();
        if (hostId == null) {
            log.warn("hostId 가 없음, event={}", event);
            return;
        }

        String title = templateRegistry.title(NotificationType.GROUPBUY_STATUS_ENDED);
        String body = templateRegistry.body(NotificationType.GROUPBUY_STATUS_ENDED);

        NotificationPayload payload = new NotificationPayload(title, body, event);

        Notification hostNoti = notificationFactory.create(
                NotificationType.GROUPBUY_STATUS_ENDED,
                hostId,
                event,
                Map.of(
                        "groupBuyTitle", event.getGroupBuyTitle(),
                        "extraMessage","다음 공구에서 만나요!"
                        )
        );

        List<Notification> participantNotis = event.getParticipantIds().stream()
                .filter(id -> !id.equals(hostId))
                .map(id -> notificationFactory.create(
                        NotificationType.GROUPBUY_STATUS_ENDED,
                        id,
                        event,
                        Map.of(
                                "groupBuyTitle", event.getGroupBuyTitle(),
                                "extraMessage", "참여해 주셔서 감사합니다! 🎉"
                        )
                ))
                .toList();

        notificationRepository.saveAll(Stream.concat(
                Stream.of(hostNoti),
                participantNotis.stream()
        ).toList());

        emitterRepository.send(hostId.toString(),
                NotificationType.GROUPBUY_STATUS_ENDED.name(),
                payload);

        participantNotis.forEach(noti ->
                emitterRepository.send(noti.getReceiverId().toString(),
                        NotificationType.GROUPBUY_STATUS_ENDED.name(), payload));

        log.debug("✅ 공구 종료 알림 전송 완료: groupId={}", event.getGroupBuyId());
    }
}
