package com.moogsan.moongsan_backend.domain.user.exception.code;

import com.moogsan.moongsan_backend.global.exception.code.ErrorCodeType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements ErrorCodeType {

    // 400 Bad Request
    INVALID_INPUT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "잘못된 요청"),

    // 401 Unauthorized
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "로그인 필요"),

    // 403 Forbidden
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "권한 없음"),

    // 404 Not Found
    NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "찾을 수 없음"),

    // 409 Conflict
    DUPLICATE_VALUE("DUPLICATE_VALUE", HttpStatus.CONFLICT, "충돌 발생"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");

    private final String code;
    private final HttpStatus status;
    @Getter
    private final String defaultMessage;

    UserErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
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
