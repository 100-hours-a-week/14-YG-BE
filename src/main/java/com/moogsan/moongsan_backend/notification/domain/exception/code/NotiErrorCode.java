package com.moogsan.moongsan_backend.domain.notification.exception.code;

import com.moogsan.moongsan_backend.global.exception.code.ErrorCodeType;
import org.springframework.http.HttpStatus;

public enum NotiErrorCode implements ErrorCodeType {
    NOTI_NOT_FOUND("NOTI_NOT_FOUND", HttpStatus.NOT_FOUND);

    private final String code;
    private final HttpStatus status;

    NotiErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
