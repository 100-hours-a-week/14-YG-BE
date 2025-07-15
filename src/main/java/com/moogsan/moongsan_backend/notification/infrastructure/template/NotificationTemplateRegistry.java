package com.moogsan.moongsan_backend.notification.infrastructure.template;

import com.moogsan.moongsan_backend.notification.domain.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationTemplateRegistry {

    public String title(NotificationType type) {
        return type.getTitleTemplate();
    }

    public String body(NotificationType type) {
        return type.getBodyTemplate();
    }
}
