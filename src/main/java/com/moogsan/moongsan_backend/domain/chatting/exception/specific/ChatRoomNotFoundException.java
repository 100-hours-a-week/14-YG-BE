package com.moogsan.moongsan_backend.domain.chatting.exception.specific;

import com.moogsan.moongsan_backend.domain.chatting.exception.base.ChattingException;
import com.moogsan.moongsan_backend.domain.chatting.exception.code.ChattingErrorCode;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.base.GroupBuyException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.code.GroupBuyErrorCode;

import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.CHAT_ROOM_NOT_FOUND;

public class ChatRoomNotFoundException extends ChattingException {
    public ChatRoomNotFoundException() {
        super(ChattingErrorCode.CHAT_ROOM_NOT_FOUND, CHAT_ROOM_NOT_FOUND);
    }

    public ChatRoomNotFoundException(String message) {
        super(ChattingErrorCode.CHAT_ROOM_NOT_FOUND, message);
    }
}
