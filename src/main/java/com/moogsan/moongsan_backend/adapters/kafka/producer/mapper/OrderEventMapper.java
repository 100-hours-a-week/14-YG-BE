package com.moogsan.moongsan_backend.adapters.kafka.producer.mapper;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderCanceledEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderConfirmedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderPendingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderRefundedEvent;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderEventMapper {

    // 주문 생성 이벤트
    public OrderPendingEvent toPendingEvent(
            Long orderId, Long groupBuyId, Long hostId, String buyerName, int quantity
    ) {
        return OrderPendingEvent.builder()
                .orderId(orderId)
                .groupBuyId(groupBuyId)
                .hostId(hostId)
                .buyerName(buyerName)
                .quantity(quantity)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 주문 확인 이벤트
    public OrderConfirmedEvent toConfirmedEvent(Long orderId) {
        return OrderConfirmedEvent.builder()
                .orderId(orderId)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 주문 취소 이벤트
    public OrderCanceledEvent toCanceledEvent(Long orderId) {
        return OrderCanceledEvent.builder()
                .orderId(orderId)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 주문 환불 이벤트
    public OrderRefundedEvent toRefundedEvent(Long orderId) {
        return OrderRefundedEvent.builder()
                .orderId(orderId)
                .occurredAt(Instant.now().toString())
                .build();
    }
}
