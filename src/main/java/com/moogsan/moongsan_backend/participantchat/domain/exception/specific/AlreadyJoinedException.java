package com.moogsan.moongsan_backend.participantchat.domain.exception.specific;

import com.moogsan.moongsan_backend.participantchat.domain.exception.base.ChattingException;
import com.moogsan.moongsan_backend.participantchat.domain.exception.code.ChattingErrorCode;

import static com.moogsan.moongsan_backend.participantchat.domain.message.ResponseMessage.ALREADEY_JOINED;

public class AlreadyJoinedException extends ChattingException {
    public AlreadyJoinedException() {
        super(ChattingErrorCode.ALREADY_JOINED, ALREADEY_JOINED);
    }

    public AlreadyJoinedException(String message) {
        super(ChattingErrorCode.ALREADY_JOINED, message);
    }
}
