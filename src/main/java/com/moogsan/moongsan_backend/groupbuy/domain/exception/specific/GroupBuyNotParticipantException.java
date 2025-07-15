package com.moogsan.moongsan_backend.groupbuy.domain.exception.specific;

import com.moogsan.moongsan_backend.groupbuy.domain.exception.base.GroupBuyException;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.code.GroupBuyErrorCode;

import static com.moogsan.moongsan_backend.groupbuy.message.ResponseMessage.NOT_PARTICIPANT;

public class GroupBuyNotParticipantException extends GroupBuyException {
    public GroupBuyNotParticipantException() {
        super(GroupBuyErrorCode.NOT_PARTICIPANT, NOT_PARTICIPANT);
    }

    public GroupBuyNotParticipantException(String message) {
        super(GroupBuyErrorCode.NOT_PARTICIPANT, message);
    }
}
