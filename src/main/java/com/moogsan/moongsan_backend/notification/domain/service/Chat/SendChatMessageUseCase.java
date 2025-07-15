package com.moogsan.moongsan_backend.notification.domain.service.Chat;

import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyDueApproachingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.NotificationPublisher;
import com.moogsan.moongsan_backend.notification.infrastructure.template.NotificationTemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SendChatMessageUseCase {
    private final NotificationTemplateRegistry templateRegistry;
    private final NotificationPublisher notificationPublisher;


    public void handleGroupBuyDueApproaching(GroupBuyDueApproachingEvent event) {


    }
}
