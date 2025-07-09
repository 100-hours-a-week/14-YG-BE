package com.moogsan.moongsan_backend.domain.notification.template;

import com.moogsan.moongsan_backend.domain.notification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

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
