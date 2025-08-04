package com.moogsan.moongsan_backend.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SseEventEnvelope<T> {
    private String topic;
    private String type;
    private long version;
    private Instant ts;
    private T payload;
    private Map<String, Object> meta;
}
