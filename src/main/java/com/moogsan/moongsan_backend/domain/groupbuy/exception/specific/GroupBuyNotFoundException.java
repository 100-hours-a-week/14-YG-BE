package com.moogsan.moongsan_backend.domain.groupbuy.exception.specific;

import com.moogsan.moongsan_backend.domain.groupbuy.exception.base.GroupBuyException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.code.GroupBuyErrorCode;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_EXIST;

public class GroupBuyNotFoundException extends GroupBuyException {
    public GroupBuyNotFoundException() {
        super(GroupBuyErrorCode.GROUPBUY_NOT_FOUND, NOT_EXIST);
    }

    public GroupBuyNotFoundException(String message) {
        super(GroupBuyErrorCode.GROUPBUY_NOT_FOUND, message);
    }
}
