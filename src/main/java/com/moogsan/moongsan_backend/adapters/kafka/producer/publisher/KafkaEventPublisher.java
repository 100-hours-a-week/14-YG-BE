package com.moogsan.moongsan_backend.adapters.kafka.producer.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@EnableKafka
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher{

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public <T> void publish(String topic, String key, T event) {
        try {
            kafkaTemplate.send(topic, key, event).get(); // 동기 전송
            log.info("✅ [SYNC] Kafka event sent: topic={}, key={}, event={}", topic, key, event);
        } catch (Exception e) {
            log.error("❌ Kafka sync send failed", e);
        }
    }
}
