package com.moogsan.moongsan_backend.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NotificationReadStatus {

    private Boolean read;

}
