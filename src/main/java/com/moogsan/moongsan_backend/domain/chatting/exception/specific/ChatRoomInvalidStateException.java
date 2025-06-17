package com.moogsan.moongsan_backend.domain.chatting.exception.specific;

import com.moogsan.moongsan_backend.domain.chatting.exception.base.ChattingException;
import com.moogsan.moongsan_backend.domain.chatting.exception.code.ChattingErrorCode;

import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.CHAT_ROOM_INVALID_STATE;

public class ChatRoomInvalidStateException extends ChattingException {
    public ChatRoomInvalidStateException() {
        super(ChattingErrorCode.CHAT_ROOM_INVALID_STATE, CHAT_ROOM_INVALID_STATE);
    }

    public ChatRoomInvalidStateException(String message) {
        super(ChattingErrorCode.CHAT_ROOM_INVALID_STATE, message);
    }
}
