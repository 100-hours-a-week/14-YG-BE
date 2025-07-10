package com.moogsan.moongsan_backend.global.exception.specific;

import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;

import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

public class SerializationException  extends BusinessException {
    public SerializationException() {
        super(ErrorCode.SERIALIZATION_FAILED, SERIALIZATION_FAIL);
    }

    public SerializationException(String message) {
        super(ErrorCode.SERIALIZATION_FAILED, message);
    }
}
