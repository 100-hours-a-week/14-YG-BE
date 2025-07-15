package com.moogsan.moongsan_backend.participantchat.domain.exception.specific;

import com.moogsan.moongsan_backend.participantchat.domain.exception.base.ChattingException;
import com.moogsan.moongsan_backend.participantchat.domain.exception.code.ChattingErrorCode;

import static com.moogsan.moongsan_backend.participantchat.domain.message.ResponseMessage.CHAT_ROOM_INVALID_STATE;

public class ChatRoomInvalidStateException extends ChattingException {
    public ChatRoomInvalidStateException() {
        super(ChattingErrorCode.CHAT_ROOM_INVALID_STATE, CHAT_ROOM_INVALID_STATE);
    }

    public ChatRoomInvalidStateException(String message) {
        super(ChattingErrorCode.CHAT_ROOM_INVALID_STATE, message);
    }
}
