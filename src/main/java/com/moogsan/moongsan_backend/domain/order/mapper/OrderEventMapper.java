package com.moogsan.moongsan_backend.domain.order.mapper;

import com.moogsan.moongsan_backend.domain.order.event.OrderCanceledEvent;
import com.moogsan.moongsan_backend.domain.order.event.OrderConfirmedEvent;
import com.moogsan.moongsan_backend.domain.order.event.OrderPendingEvent;
import com.moogsan.moongsan_backend.domain.order.event.OrderRefundedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderEventMapper {

    // 주문 생성 이벤트
    public OrderPendingEvent toPendingEvent(
            Long orderId, Long groupBuyId, String title, Long hostId, String buyerName, int quantity
    ) {
        return OrderPendingEvent.builder()
                .orderId(orderId)
                .groupBuyId(groupBuyId)
                .title(title)
                .hostId(hostId)
                .buyerName(buyerName)
                .quantity(quantity)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 주문 확인 이벤트
    public OrderConfirmedEvent toConfirmedEvent(
            Long orderId, Long groupBuyId, String title, Long participantId, String buyerName, String groupBuyName
    ) {
        return OrderConfirmedEvent.builder()
                .orderId(orderId)
                .groupBuyId(groupBuyId)
                .title(title)
                .participantId(participantId)
                .buyerName(buyerName)
                .groupBuyName(groupBuyName)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 주문 취소 이벤트
    public OrderCanceledEvent toCanceledEvent(
            Long orderId, Long groupBuyId, String title, Long hostId, String buyerName, String buyerBank, String buyerAccount, int price

    ) {
        return OrderCanceledEvent.builder()
                .orderId(orderId)
                .groupBuyId(groupBuyId)
                .title(title)
                .hostId(hostId)
                .buyerName(buyerName)
                .buyerBank(buyerBank)
                .buyerAccount(buyerAccount)
                .price(price)
                .occurredAt(Instant.now().toString())
                .build();
    }

    // 주문 환불 이벤트
    public OrderRefundedEvent toRefundedEvent(
            Long orderId, Long groupBuyId, String title, Long participantId, String buyerName, String groupBuyName
    ) {
        return OrderRefundedEvent.builder()
                .orderId(orderId)
                .groupBuyId(groupBuyId)
                .title(title)
                .participantId(participantId)
                .buyerName(buyerName)
                .groupBuyName(groupBuyName)
                .occurredAt(Instant.now().toString())
                .build();
    }
}
