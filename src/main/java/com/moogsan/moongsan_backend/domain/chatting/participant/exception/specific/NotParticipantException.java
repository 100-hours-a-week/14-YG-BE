package com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific;

import com.moogsan.moongsan_backend.domain.chatting.participant.exception.base.ChattingException;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.code.ChattingErrorCode;

import static com.moogsan.moongsan_backend.domain.chatting.participant.message.ResponseMessage.NOT_PARTICIPANT;

public class NotParticipantException extends ChattingException {
    public NotParticipantException() {
        super(ChattingErrorCode.NOT_PARTICIPANT, NOT_PARTICIPANT);
    }

    public NotParticipantException(String message) {
        super(ChattingErrorCode.NOT_PARTICIPANT, message);
    }
}
