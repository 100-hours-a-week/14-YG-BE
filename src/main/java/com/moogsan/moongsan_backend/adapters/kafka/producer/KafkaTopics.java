package com.moogsan.moongsan_backend.adapters.kafka.producer;

public interface KafkaTopics {

    // 채팅 관련 이벤트
    String CHAT_PART_MESSAGE_CREATED = "chat.part.message.created";
    String CHAT_ANON_MESSAGE_CREATED = "chat.anon.message.created";

    // 사용자 관련 이벤트
    String USER_PROFILE_UPDATED = "user.profile.updated";

    // 공동구매 관련 이벤트
    String GROUPBUY_PICKUP_UPDATED = "groupbuy.pickup.updated";
    String GROUPBUY_PICKUP_APPROACHING = "groupbuy.pickup.approaching";
    String GROUPBUY_DUE_APPROACHING = "groupbuy.due.approaching";
    String GROUPBUY_STATUS_CLOSED = "groupbuy.status.closed";
    String GROUPBUY_STATUS_FINALIZED = "groupbuy.status.finalized";
    String GROUPBUY_STATUS_ENDED = "groupbuy.status.ended";

    // 주문 상태 관련 이벤트
    String ORDER_STATUS_PENDING = "order.status.pending";
    String ORDER_STATUS_CONFIRMED = "order.status.confirmed";
    String ORDER_STATUS_CANCELED = "order.status.canceled";
    String ORDER_STATUS_REFUNDED = "order.status.refunded";
}
