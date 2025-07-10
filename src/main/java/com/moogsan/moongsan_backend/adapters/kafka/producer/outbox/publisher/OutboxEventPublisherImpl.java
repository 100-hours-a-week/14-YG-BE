package com.moogsan.moongsan_backend.adapters.kafka.producer.outbox.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.outbox.OutboxEventEntity;
import com.moogsan.moongsan_backend.adapters.kafka.producer.outbox.OutboxEventRepository;
import com.moogsan.moongsan_backend.adapters.kafka.producer.outbox.OutboxEventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisherImpl implements OutboxEventPublisher {
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional  // 반드시 도메인 트랜잭션 안에서 호출되어야 함
    public <T> void publish(String aggregateType,
                            String aggregateId,
                            String topic,
                            String key,
                            T payload) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(payload);

        OutboxEventEntity ev = OutboxEventEntity.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .kafkaTopic(topic)
                .kafkaPartitionKey(key)
                .payload(json)
                .headers("{}")
                .status(OutboxEventStatus.PENDING)
                .nextRetryAt(LocalDateTime.now())
                .build();

        outboxRepo.save(ev);
    }
}

