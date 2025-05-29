package com.moogsan.moongsan_backend.domain.chatting.exception.specific;

import com.moogsan.moongsan_backend.domain.chatting.exception.base.ChattingException;
import com.moogsan.moongsan_backend.domain.chatting.exception.code.ChattingErrorCode;

public class NotParticipantException extends ChattingException {
    public NotParticipantException() {
        super(ChattingErrorCode.NOT_PARTICIPANT, "참여자만 요청가능합니다.");
    }

    public NotParticipantException(String message) {
        super(ChattingErrorCode.NOT_PARTICIPANT, message);
    }
}
