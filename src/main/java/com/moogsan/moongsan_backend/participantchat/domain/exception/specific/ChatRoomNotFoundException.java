package com.moogsan.moongsan_backend.participantchat.domain.exception.specific;

import com.moogsan.moongsan_backend.participantchat.domain.exception.base.ChattingException;
import com.moogsan.moongsan_backend.participantchat.domain.exception.code.ChattingErrorCode;

import static com.moogsan.moongsan_backend.participantchat.domain.message.ResponseMessage.CHAT_ROOM_NOT_FOUND;

public class ChatRoomNotFoundException extends ChattingException {
    public ChatRoomNotFoundException() {
        super(ChattingErrorCode.CHAT_ROOM_NOT_FOUND, CHAT_ROOM_NOT_FOUND);
    }

    public ChatRoomNotFoundException(String message) {
        super(ChattingErrorCode.CHAT_ROOM_NOT_FOUND, message);
    }
}
