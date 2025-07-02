package com.moogsan.moongsan_backend.global.outbox;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
    DEAD
}
