package com.moogsan.moongsan_backend.adapters.kafka.producer.outbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "kafka_topic", nullable = false, length = 100)
    private String kafkaTopic;

    @Column(name = "kafka_partition_key", length = 100)
    private String kafkaPartitionKey;

    @Column(columnDefinition = "json", nullable = false)
    private String payload;

    @Column(columnDefinition = "json")
    private String headers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status = OutboxEventStatus.PENDING;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt = LocalDateTime.now();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
