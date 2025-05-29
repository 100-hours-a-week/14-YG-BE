package com.moogsan.moongsan_backend.global.exception.code;

import org.springframework.http.HttpStatus;

public enum ErrorCode implements ErrorCodeType{

    INVALID_JSON("INVALID_JSON", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.UNPROCESSABLE_ENTITY),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND),
    DUPLICATE_REQUEST("DUPLICATE_REQUEST", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
