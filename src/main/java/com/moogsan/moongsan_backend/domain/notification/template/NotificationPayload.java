package com.moogsan.moongsan_backend.domain.notification.template;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationPayload {

    private String title;
    private String body;
    private Object data;
}
