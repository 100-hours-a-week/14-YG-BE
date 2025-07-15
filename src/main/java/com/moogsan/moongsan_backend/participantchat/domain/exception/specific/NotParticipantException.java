package com.moogsan.moongsan_backend.participantchat.domain.exception.specific;

import com.moogsan.moongsan_backend.participantchat.domain.exception.base.ChattingException;
import com.moogsan.moongsan_backend.participantchat.domain.exception.code.ChattingErrorCode;

import static com.moogsan.moongsan_backend.participantchat.domain.message.ResponseMessage.NOT_PARTICIPANT;

public class NotParticipantException extends ChattingException {
    public NotParticipantException() {
        super(ChattingErrorCode.NOT_PARTICIPANT, NOT_PARTICIPANT);
    }

    public NotParticipantException(String message) {
        super(ChattingErrorCode.NOT_PARTICIPANT, message);
    }
}
