package com.moogsan.moongsan_backend.domain.chatting.exception.specific;

import com.moogsan.moongsan_backend.domain.chatting.exception.base.ChattingException;
import com.moogsan.moongsan_backend.domain.chatting.exception.code.ChattingErrorCode;

public class AlreadyJoinedException extends ChattingException {
    public AlreadyJoinedException() {
        super(ChattingErrorCode.ALREADY_JOINED, "중복된 참여 요청입니다.");
    }

    public AlreadyJoinedException(String message) {
        super(ChattingErrorCode.ALREADY_JOINED, message);
    }
}
