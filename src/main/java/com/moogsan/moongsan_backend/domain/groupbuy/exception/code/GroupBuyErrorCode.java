package com.moogsan.moongsan_backend.domain.groupbuy.exception;

import com.moogsan.moongsan_backend.global.exception.ErrorCodeType;
import org.springframework.http.HttpStatus;

public enum GroupBuyErrorCode implements ErrorCodeType {
    NOT_PARTICIPANT("NOT_PARTICIPANT", HttpStatus.FORBIDDEN),        // 참여자 아님
    NOT_HOST("NOT_HOST", HttpStatus.FORBIDDEN),                      // 주최자 아님
    GROUPBUY_NOT_FOUND("GROUPBUY_NOT_FOUND", HttpStatus.NOT_FOUND),  // 존재하지 않는 공구 게시글
    ALREADY_CANCELED("ALREADY_CANCELED", HttpStatus.CONFLICT), // 이미 취소된 주문
    INVALID_STATE("INVALID_GROUPBUY_STATE", HttpStatus.CONFLICT); // OPEN status가 아님

    private final String code;
    private final HttpStatus status;

    GroupBuyErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
