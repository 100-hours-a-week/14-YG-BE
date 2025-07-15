package com.moogsan.moongsan_backend.global.infrastructure.kafka.publisher;

public interface EventPublisher {
    <T> void publish(String topic, String key, T event);
}
