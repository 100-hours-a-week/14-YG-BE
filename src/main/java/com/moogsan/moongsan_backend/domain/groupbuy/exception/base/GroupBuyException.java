package com.moogsan.moongsan_backend.domain.groupbuy.exception;

import com.moogsan.moongsan_backend.domain.groupbuy.exception.code.GroupBuyErrorCode;
import com.moogsan.moongsan_backend.global.exception.BusinessException;

import java.util.Map;

public class GroupBuyException extends BusinessException {
    public GroupBuyException(GroupBuyErrorCode errorCode) {
        super(errorCode);
    }

    public GroupBuyException(GroupBuyErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public GroupBuyException(GroupBuyErrorCode errorCode, Map<String, Object> parameters) {
        super(errorCode, parameters);
    }

    public GroupBuyException(GroupBuyErrorCode errorCode, String message, Map<String, Object> parameters) {
        super(errorCode, message, parameters);
    }
}
