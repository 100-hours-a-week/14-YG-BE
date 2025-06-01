package com.moogsan.moongsan_backend.domain.order.exception.code;

import com.moogsan.moongsan_backend.global.exception.code.ErrorCodeType;
import org.springframework.http.HttpStatus;

public enum OrderErrorCode implements ErrorCodeType {
    ORDER_NOT_FOUND("NOT_FOUNDED", HttpStatus.FORBIDDEN),
    INVALID_STATE("INVALID_STATE", HttpStatus.CONFLICT);

    private final String code;
    private final HttpStatus status;

    OrderErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}
