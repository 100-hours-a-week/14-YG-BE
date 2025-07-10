package com.moogsan.moongsan_backend.domain.notification.exception.specific;

import com.moogsan.moongsan_backend.domain.notification.exception.base.NotiException;
import com.moogsan.moongsan_backend.domain.notification.exception.code.NotiErrorCode;

import static com.moogsan.moongsan_backend.domain.notification.exception.code.NotiErrorCode.NOTI_NOT_FOUND;
import static com.moogsan.moongsan_backend.domain.notification.message.ResponseMessage.NOTIFICATION_NOT_FOUND;

public class NotiNotFoundException extends NotiException {
    public NotiNotFoundException() {
        super(NOTI_NOT_FOUND, NOTIFICATION_NOT_FOUND);
    }

    public NotiNotFoundException(String message) {
        super(NOTI_NOT_FOUND, message);
    }
}
