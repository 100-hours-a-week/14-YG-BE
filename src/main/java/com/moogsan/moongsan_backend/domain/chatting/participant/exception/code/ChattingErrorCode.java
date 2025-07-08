package com.moogsan.moongsan_backend.domain.chatting.participant.exception.code;

import com.moogsan.moongsan_backend.global.exception.code.ErrorCodeType;
import org.springframework.http.HttpStatus;

public enum ChattingErrorCode implements ErrorCodeType {
    CHAT_ROOM_NOT_FOUND("CHAT_ROOM_NOT_FOUND", HttpStatus.NOT_FOUND),
    ALREADY_JOINED("ALREADY_JOINED_PARTICIPANT", HttpStatus.CONFLICT),
    NOT_PARTICIPANT("NOT_PARTICIPANT", HttpStatus.FORBIDDEN),
    CHAT_ROOM_INVALID_STATE("CHAT_ROOM_INVALID_STATE", HttpStatus.BAD_REQUEST);

    private final String code;
    private final HttpStatus status;

    ChattingErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
