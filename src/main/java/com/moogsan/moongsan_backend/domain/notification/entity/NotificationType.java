package com.moogsan.moongsan_backend.domain.notification.entity;

import com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics;
import lombok.Getter;

@Getter
public enum NotificationType {

    // ───────── 주문 상태 ─────────
    ORDER_PENDING(
            KafkaTopics.ORDER_STATUS_PENDING,
            "주문이 들어왔어요!",
            "{buyerName}님이 {qty}개를 주문했습니다."
    ),

    ORDER_CONFIRMED(
            KafkaTopics.ORDER_STATUS_CONFIRMED,
            "주문이 확정됐어요!",
            "{groupBuyTitle}: {buyerName}님의 주문이 확정됐습니다."
    ),

    ORDER_CANCELED(
            KafkaTopics.ORDER_STATUS_CANCELED,
            "주문이 취소됐어요!",
            "{buyerName}님의 주문이 취소되었습니다.\n" +
                    "■ 환불 계좌  : {buyerBank} {buyerAccount}\n" +
                    "■ 환불 금액  : {price}원\n\n" +
                    "영업일 1일 이내로 환불을 진행해 주세요."
    ),

    ORDER_REFUNDED(
            KafkaTopics.ORDER_STATUS_REFUNDED,
            "환불이 완료됐어요!",
            "{groupBuyTitle}: {buyerName}님의 주문에 대한 환불이 완료됐습니다."
    ),

    // ──────── 공동구매 상태 ────────
    GROUPBUY_PICKUP_UPDATED(
            KafkaTopics.GROUPBUY_PICKUP_UPDATED,
            "픽업 일정 변경",
            "새 픽업일: {pickupDate}, 변경 사유: {dateModificationReason}"
    );

    // ───────── 필드 ─────────
    private final String topic;          // 실제 Kafka 토픽명
    private final String titleTemplate;  // 기본 제목
    private final String bodyTemplate;   // 기본 본문

    // ───────── 생성자 ─────────
    NotificationType(String topic, String title, String body) {
        this.topic         = topic;
        this.titleTemplate = title;
        this.bodyTemplate  = body;
    }

    /** 토픽명 → enum 역매핑 (컨슈머에서 사용) */
    public static NotificationType fromTopic(String topic) {
        for (NotificationType t : values()) {
            if (t.topic.equals(topic)) return t;
        }
        throw new IllegalArgumentException("Unknown topic: " + topic);
    }
}
