package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토픽: order.status.canceled
 * 설명: 주문 취소 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCanceledEvent extends BaseEvent{
    private Long orderId;  // 주문 아이디
}
