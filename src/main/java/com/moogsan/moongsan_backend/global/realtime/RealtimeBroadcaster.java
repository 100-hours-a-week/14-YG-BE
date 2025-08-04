package com.moogsan.moongsan_backend.global.realtime;

import com.moogsan.moongsan_backend.global.dto.SseEventEnvelope;
import com.moogsan.moongsan_backend.global.infrastructure.sse.SseHub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SseRealtimeBroadcaster {

    private final SseHub sseHub;

    public <T> void publish(String topic, String type, long version, T payload) {
        var envelope = SseEventEnvelope.<T>builder()
                .topic(topic)
                .type(type)
                .version(version)
                .ts(Instant.now())
                .payload(payload)
                .build();
        sseHub.publish(topic, type, envelope);
    }
}
