package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 토픽: order.status.confirmed
 * 설명: 주문 확인 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderConfirmedEvent extends BaseEvent{
    private Long orderId;  // 주문 아이디
}
