package com.moogsan.moongsan_backend.domain.chatting.exception.specific;

import com.moogsan.moongsan_backend.domain.chatting.exception.base.ChattingException;
import com.moogsan.moongsan_backend.domain.chatting.exception.code.ChattingErrorCode;

public class ChatRoomInvalidStateException extends ChattingException {
    public ChatRoomInvalidStateException() {
        super(ChattingErrorCode.CHAT_ROOM_INVALID_STATE, "채팅방 상태가 유효하지 않아 요청을 처리할 수 없습니다.");
    }

    public ChatRoomInvalidStateException(String message) {
        super(ChattingErrorCode.CHAT_ROOM_INVALID_STATE, message);
    }
}
