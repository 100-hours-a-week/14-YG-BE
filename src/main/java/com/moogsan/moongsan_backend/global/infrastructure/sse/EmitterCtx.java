package com.moogsan.moongsan_backend.global.infrastructure.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

public record EmitterCtx(
        String connectionId,
        Long userId,
        SseEmitter emitter,
        Set<String> topics
) {}
