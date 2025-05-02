package com.moogsan.moongsan_backend.domain.order.exception.specific;

import com.moogsan.moongsan_backend.domain.order.exception.base.OrderException;
import com.moogsan.moongsan_backend.domain.order.exception.code.OrderErrorCode;

public class OrderNotFoundException extends OrderException {
    public OrderNotFoundException() {
        super(OrderErrorCode.ORDER_NOT_FOUNDED, "존재하지 않는 주문입니다.");
    }

    public OrderNotFoundException(String message) {
        super(OrderErrorCode.ORDER_NOT_FOUNDED, message);
    }
}
