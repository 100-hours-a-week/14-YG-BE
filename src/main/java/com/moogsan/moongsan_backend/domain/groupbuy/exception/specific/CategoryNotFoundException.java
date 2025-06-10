package com.moogsan.moongsan_backend.domain.groupbuy.exception.specific;

import com.moogsan.moongsan_backend.domain.groupbuy.exception.base.GroupBuyException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.code.GroupBuyErrorCode;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.GroupBuyResponseMessage.NOT_EXIST_CATEGORY;

public class CategoryNotFoundException extends GroupBuyException {
    public CategoryNotFoundException() {
        super(GroupBuyErrorCode.CATEGORY_NOT_FOUND, NOT_EXIST_CATEGORY);
    }

    public CategoryNotFoundException(String message) {
        super(GroupBuyErrorCode.CATEGORY_NOT_FOUND, message);
    }
}

