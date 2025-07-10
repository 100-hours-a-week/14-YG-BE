package com.moogsan.moongsan_backend.domain.notification.exception.base;

import com.moogsan.moongsan_backend.domain.chatting.participant.exception.code.ChattingErrorCode;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.code.GroupBuyErrorCode;
import com.moogsan.moongsan_backend.domain.notification.exception.code.NotiErrorCode;
import com.moogsan.moongsan_backend.global.exception.base.BusinessException;

import java.util.Map;

public class NotiException extends BusinessException {
    public NotiException(NotiErrorCode errorCode) {
        super(errorCode);
    }

    public NotiException(NotiErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public NotiException(NotiErrorCode errorCode, Map<String, Object> parameters) {
        super(errorCode, parameters);
    }

    public NotiException(NotiErrorCode errorCode, String message, Map<String, Object> parameters) {
        super(errorCode, message, parameters);
    }
}
