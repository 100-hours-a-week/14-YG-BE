package com.moogsan.moongsan_backend.global.exception.specific;

import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;

import static com.moogsan.moongsan_backend.global.message.ResponseMessage.UNAUTHENTICATED;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED, UNAUTHENTICATED);
    }
}
