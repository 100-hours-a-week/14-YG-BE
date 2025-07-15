package com.moogsan.moongsan_backend.notification.domain.exception.specific;

import com.moogsan.moongsan_backend.notification.domain.exception.base.NotiException;

import static com.moogsan.moongsan_backend.notification.domain.exception.code.NotiErrorCode.NOTI_NOT_FOUND;
import static com.moogsan.moongsan_backend.notification.domain.message.ResponseMessage.NOTIFICATION_NOT_FOUND;

public class NotiNotFoundException extends NotiException {
    public NotiNotFoundException() {
        super(NOTI_NOT_FOUND, NOTIFICATION_NOT_FOUND);
    }

    public NotiNotFoundException(String message) {
        super(NOTI_NOT_FOUND, message);
    }
}
