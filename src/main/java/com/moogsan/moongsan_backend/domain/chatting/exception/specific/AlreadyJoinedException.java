package com.moogsan.moongsan_backend.domain.chatting.exception.specific;

import com.moogsan.moongsan_backend.domain.chatting.exception.base.ChattingException;
import com.moogsan.moongsan_backend.domain.chatting.exception.code.ChattingErrorCode;

import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.ALREADEY_JOINED;

public class AlreadyJoinedException extends ChattingException {
    public AlreadyJoinedException() {
        super(ChattingErrorCode.ALREADY_JOINED, ALREADEY_JOINED);
    }

    public AlreadyJoinedException(String message) {
        super(ChattingErrorCode.ALREADY_JOINED, message);
    }
}
