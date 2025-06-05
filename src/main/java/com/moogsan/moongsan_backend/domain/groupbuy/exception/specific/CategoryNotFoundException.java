package com.moogsan.moongsan_backend.domain.groupbuy.exception.specific;

import com.moogsan.moongsan_backend.domain.groupbuy.exception.base.GroupBuyException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.code.GroupBuyErrorCode;

public class CategoryNotFoundException extends GroupBuyException {
    public CategoryNotFoundException() {
        super(GroupBuyErrorCode.CATEGORY_NOT_FOUND, "존재하지 않는 카테고리입니다.");
    }

    public CategoryNotFoundException(String message) {
        super(GroupBuyErrorCode.CATEGORY_NOT_FOUND, message);
    }
}

