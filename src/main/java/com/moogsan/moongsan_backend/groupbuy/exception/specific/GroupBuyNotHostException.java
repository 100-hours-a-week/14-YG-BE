package com.moogsan.moongsan_backend.groupbuy.exception.specific;

import com.moogsan.moongsan_backend.groupbuy.exception.base.GroupBuyException;
import com.moogsan.moongsan_backend.groupbuy.exception.code.GroupBuyErrorCode;

import static com.moogsan.moongsan_backend.groupbuy.message.ResponseMessage.NOT_HOST;

public class GroupBuyNotHostException extends GroupBuyException {
    public GroupBuyNotHostException() {
        super(GroupBuyErrorCode.NOT_HOST, NOT_HOST);
    }

    public GroupBuyNotHostException(String message) {
        super(GroupBuyErrorCode.NOT_HOST, message);
    }
}
