package com.moogsan.moongsan_backend.domain.chatting.exception.code;

import com.moogsan.moongsan_backend.global.exception.code.ErrorCodeType;
import org.springframework.http.HttpStatus;

public enum ChattingErrorCode implements ErrorCodeType {
    CHAT_ROOM_NOT_FOUND("CHAT_ROOM_NOT_FOUND", HttpStatus.NOT_FOUND);

    private final String code;
    private final HttpStatus status;

    ChattingErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
