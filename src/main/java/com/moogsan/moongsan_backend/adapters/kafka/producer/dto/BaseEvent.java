package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 모든 Kafka 이벤트 DTO의 공통 필드 정의
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseEvent {
    private String occurredAt;  // 이벤트 실제 발생 시간 (ISO 8601)
}
