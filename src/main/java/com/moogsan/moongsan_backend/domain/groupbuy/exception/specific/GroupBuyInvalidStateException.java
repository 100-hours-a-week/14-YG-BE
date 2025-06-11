package com.moogsan.moongsan_backend.domain.groupbuy.exception.specific;

import com.moogsan.moongsan_backend.domain.groupbuy.exception.base.GroupBuyException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.code.GroupBuyErrorCode;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.AFTER_ENDED;

public class GroupBuyInvalidStateException extends GroupBuyException {
    public GroupBuyInvalidStateException() {
        super(GroupBuyErrorCode.INVALID_STATE, AFTER_ENDED);
    }

    public GroupBuyInvalidStateException(String message) {
        super(GroupBuyErrorCode.INVALID_STATE, message);
    }
}
