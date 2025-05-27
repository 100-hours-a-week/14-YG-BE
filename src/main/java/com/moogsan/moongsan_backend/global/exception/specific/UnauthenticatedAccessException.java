package com.moogsan.moongsan_backend.global.exception.specific;

import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;

public class UnauthenticatedAccessException  extends BusinessException {
    public UnauthenticatedAccessException() {
        super(ErrorCode.UNAUTHORIZED, "인증 정보가 유효하지 않습니다.");
    }

    public UnauthenticatedAccessException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
