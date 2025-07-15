package com.moogsan.moongsan_backend.groupbuy.application.port;

import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyUpdatedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.RealtimePublisher;
import com.moogsan.moongsan_backend.notification.infrastructure.template.NotificationTemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BroadcastGroupBuyUpdatedEventUseCase {

    private final NotificationTemplateRegistry templateRegistry;
    private final RealtimePublisher realtimePublisher;

    public void handleGroupBuyUpdated(GroupBuyUpdatedEvent event) {
        if (event == null || event.getGroupBuyId() == null) {
            log.warn("⚠️  GroupBuyUpdatedEvent 잘못된 페이로드: {}", event);
            return;
        }

        // 실시간 브로드캐스트 (Thin Event)로 처리함
        realtimePublisher.publish(event);

        log.debug("📡 GroupBuyUpdatedEvent 브로드캐스트 완료: groupBuyId={}", event.getGroupBuyId());
    }

}
