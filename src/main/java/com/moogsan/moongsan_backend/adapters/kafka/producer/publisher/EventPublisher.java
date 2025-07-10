package com.moogsan.moongsan_backend.adapters.kafka.producer.publisher;

public interface EventPublisher {
    <T> void publish(String topic, String key, T event);
}
