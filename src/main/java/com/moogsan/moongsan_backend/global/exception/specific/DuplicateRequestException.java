package com.moogsan.moongsan_backend.global.exception.specific;

import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;

public class DuplicateRequestException extends BusinessException {
    public DuplicateRequestException() {
        super(ErrorCode.DUPLICATE_REQUEST, "중복 요청입니다. 잠시 후 다시 시도해주세요.");
    }

    public DuplicateRequestException(String message) {
        super(ErrorCode.DUPLICATE_REQUEST, message);
    }
}
