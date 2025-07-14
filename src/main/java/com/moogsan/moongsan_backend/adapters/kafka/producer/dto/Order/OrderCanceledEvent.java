package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 토픽: order.status.canceled
 * 설명: 주문 취소 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderCanceledEvent extends BaseEvent{
    private Long orderId;          // 주문 아이디
    private Long groupBuyId;       // 공동구매 게시글 아이디
    private Long hostId;           // 주최자 아이디
    private String buyerName;      // 구매자 성명
    private String buyerBank;      // 구매자 은행
    private String buyerAccount;   // 구매자 계좌 번호
    private int price;             // 환불 금액
}
