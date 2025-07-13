package com.moogsan.moongsan_backend.adapters.kafka.producer.outbox.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface OutboxEventPublisher {
    <T> void publish(
            String aggregateType,      // ex. "ORDER"
            String aggregateId,        // ex. orderId.toString()
            String topic,              // ex. KafkaTopics.ORDER_STATUS_CONFIRMED
            String key,                // ex. orderId.toString()
            T payload                  // event DTO
    ) throws JsonProcessingException;
}
