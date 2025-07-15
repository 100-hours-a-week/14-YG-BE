package com.moogsan.moongsan_backend.global.infrastructure.kafka.outbox;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
    DEAD
}
