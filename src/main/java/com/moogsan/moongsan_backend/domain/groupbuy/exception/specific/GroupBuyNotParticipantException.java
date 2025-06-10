package com.moogsan.moongsan_backend.domain.groupbuy.exception.specific;

import com.moogsan.moongsan_backend.domain.groupbuy.exception.base.GroupBuyException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.code.GroupBuyErrorCode;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.GroupBuyResponseMessage.NOT_PARTICIPANT;

public class GroupBuyNotParticipantException extends GroupBuyException {
    public GroupBuyNotParticipantException() {
        super(GroupBuyErrorCode.NOT_PARTICIPANT, NOT_PARTICIPANT);
    }

    public GroupBuyNotParticipantException(String message) {
        super(GroupBuyErrorCode.NOT_PARTICIPANT, message);
    }
}