package com.moogsan.moongsan_backend.adapters.kafka.producer.mapper;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderCanceledEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderConfirmedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderPendingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderRefundedEvent;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderEventMapper {

    // 주문 생성 이벤트
    public OrderPendingEvent toPendingEvent(Order order) {
        return OrderPendingEvent.builder()
                .orderId(order.getId())
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 주문 확인 이벤트
    public OrderConfirmedEvent toConfirmedEvent(Order order) {
        return OrderConfirmedEvent.builder()
                .orderId(order.getId())
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 주문 취소 이벤트
    public OrderCanceledEvent toCanceledEvent(Order order) {
        return OrderCanceledEvent.builder()
                .orderId(order.getId())
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 주문 환불 이벤트
    public OrderRefundedEvent toRefundedEvent(Order order) {
        return OrderRefundedEvent.builder()
                .orderId(order.getId())
                .occurredAt(Instant.now().toString())
                .build();
    }
}
