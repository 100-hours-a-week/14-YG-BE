package com.moogsan.moongsan_backend.adapters.kafka.producer.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher{

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public <T> void publish(String topic, String key, T event) {
        kafkaTemplate.send(topic, key, event);
    }
}
