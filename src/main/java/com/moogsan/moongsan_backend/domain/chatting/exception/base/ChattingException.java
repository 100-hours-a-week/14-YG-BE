package com.moogsan.moongsan_backend.domain.chatting.exception.base;

import com.moogsan.moongsan_backend.domain.chatting.exception.code.ChattingErrorCode;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.code.GroupBuyErrorCode;
import com.moogsan.moongsan_backend.global.exception.base.BusinessException;

import java.util.Map;

public class ChattingException extends BusinessException {
    public ChattingException(GroupBuyErrorCode errorCode) {
        super(errorCode);
    }

    public ChattingException(ChattingErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ChattingException(GroupBuyErrorCode errorCode, Map<String, Object> parameters) {
        super(errorCode, parameters);
    }

    public ChattingException(GroupBuyErrorCode errorCode, String message, Map<String, Object> parameters) {
        super(errorCode, message, parameters);
    }
}
