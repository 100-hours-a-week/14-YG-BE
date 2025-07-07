package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 토픽: order.status.pending
 * 설명: 주문 생성 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderPendingEvent extends BaseEvent{
    private Long orderId;      // 주문 아이디
    private Long groupBuyId;   // 공동구매 게시글 아이디
    private Long hostId;       // 공동구매 호스트 아이디
    private String buyerName;  // 구매자 성명
    private int quantity;      // 구매 수량
}
