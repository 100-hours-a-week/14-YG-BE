package com.moogsan.moongsan_backend.domain.notification.entity;

import com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics;
import lombok.Getter;

@Getter
public enum NotificationType {

    // ───────── 주문 상태 ─────────
    ORDER_PENDING(
            KafkaTopics.ORDER_STATUS_PENDING,
            "주문이 들어왔어요!",
            "{buyerName}님이 {qty}개를 주문했어요. 입금 확인 후 주문을 확정해주세요!"
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
    GROUPBUY_STATUS_CLOSED(
            KafkaTopics.GROUPBUY_STATUS_CLOSED,
            "공구 참여자 모집이 마감됐어요!",
            "{groupBuyTitle}: 모집이 마감됐습니다!\n" +
                    "■ 총 참여 인원 : {participantCount}명\n" +
                    "■ 총 주문 수량 : {totalQty}개\n\n"
    ),

    GROUPBUY_STATUS_FINALIZED(
            KafkaTopics.GROUPBUY_STATUS_FINALIZED,
            "공구가 확정되었어요!",
            "{groupBuyTitle}: 모든 주문이 확인되어 공구가 확정되었습니다.\n" +
                    "■ 확정 참여 인원 : {participantCount}명\n" +
                    "■ 총 주문 수량 : {totalQty}개\n\n" +
                    "함께 뭉쳐주셔서 감사합니다!"
    ),

    GROUPBUY_STATUS_ENDED(
            KafkaTopics.GROUPBUY_STATUS_ENDED,
            "공구가 종료됐어요!",
            "{groupBuyTitle}: 공구가 최종 종료되었습니다. {extraMessage}\n" +
                    "공구는 만족스러우셨나요? 다음 공구에서도 뭉티기가 되어 주세요!"
    ),

    GROUPBUY_DUE_APPROACHING(
            KafkaTopics.GROUPBUY_DUE_APPROACHING,
            "마감 D-1! 내일 자정에 종료됩니다 🕛",
            "{groupBuyTitle}: 모집 마감이 하루 남았어요.{extraMessage}\n" +
                    "■ 현재 참여 인원 : {participantCount}명\n" +
                    "■ 남은 주문 수량 : {LeftQty}개\n\n" +
                    "필요하다면 오늘 안에 수량을 조정하시거나\n" +
                    "친구에게 소식을 살짝 전해 보세요 😉"
    ),

    GROUPBUY_PICKUP_APPROACHING(
            KafkaTopics.GROUPBUY_PICKUP_APPROACHING,
            "픽업 D-1! 내일 수령을 준비해주세요 📦",
            "{groupBuyTitle}: 상품 수령일이 내일입니다.{extraMessage}\n" +
                    "■ 참여 인원 : {participantCount}명\n" +
                    "■ 총 주문 수량 : {totalQty}개\n\n" +
                    "수령 장소·시간을 다시 한 번 확인하시고,\n" +
                    "문의 사항은 채팅방에 남겨주세요. 감사합니다!"
    ),

    GROUPBUY_PICKUP_UPDATED(
            KafkaTopics.GROUPBUY_PICKUP_UPDATED,
            "픽업 일정이 변경되었어요!",
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
