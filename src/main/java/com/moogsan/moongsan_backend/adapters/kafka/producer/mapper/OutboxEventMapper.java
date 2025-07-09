package com.moogsan.moongsan_backend.adapters.kafka.producer.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.outbox.OutboxEventEntity;
import com.moogsan.moongsan_backend.adapters.kafka.producer.outbox.OutboxEventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Component
@RequiredArgsConstructor
public class OutboxEventMapper {

    private final ObjectMapper objectMapper;

    public OutboxEventEntity toOutboxEventEntity(
            String topic,
            String key,
            Object eventDto,
            Map<String, String> headers
    ) {
        try {
            String payload = objectMapper.writeValueAsString(eventDto);
            return OutboxEventEntity.builder()
                    .eventId(UUID.randomUUID().toString())
                    .aggregateType(eventDto.getClass().getSimpleName())
                    .aggregateId(key)
                    .kafkaTopic(topic)
                    .kafkaPartitionKey(key)
                    .payload(payload)
                    .headers(objectMapper.writeValueAsString(headers))
                    .status(OutboxEventStatus.PENDING)
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(SERIALIZATION_FAIL, e);
        }
    }
}
