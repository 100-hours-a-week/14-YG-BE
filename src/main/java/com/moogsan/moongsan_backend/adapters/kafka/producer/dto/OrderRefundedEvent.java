package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토픽: order.status.refunded
 * 설명: 주문 환불 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRefundedEvent extends BaseEvent{
    private Long orderId;  // 주문 아이디
}
