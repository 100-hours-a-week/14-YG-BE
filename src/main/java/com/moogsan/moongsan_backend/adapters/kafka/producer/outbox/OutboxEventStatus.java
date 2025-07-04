package com.moogsan.moongsan_backend.adapters.kafka.producer.outbox;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
    DEAD
}
