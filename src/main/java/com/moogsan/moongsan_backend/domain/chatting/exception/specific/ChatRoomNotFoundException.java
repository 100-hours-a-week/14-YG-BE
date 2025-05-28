package com.moogsan.moongsan_backend.domain.chatting.exception.specific;

import com.moogsan.moongsan_backend.domain.chatting.exception.base.ChattingException;
import com.moogsan.moongsan_backend.domain.chatting.exception.code.ChattingErrorCode;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.base.GroupBuyException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.code.GroupBuyErrorCode;

public class ChatRoomNotFoundException extends ChattingException {
    public ChatRoomNotFoundException() {
        super(ChattingErrorCode.CHAT_ROOM_NOT_FOUND, "존재하지 않는 채팅방입니다.");
    }

    public ChatRoomNotFoundException(String message) {
        super(ChattingErrorCode.CHAT_ROOM_NOT_FOUND, message);
    }
}