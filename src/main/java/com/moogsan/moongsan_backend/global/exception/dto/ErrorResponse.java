package com.moogsan.moongsan_backend.global.exception.dto;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.Map;

@Builder
public record ErrorResponse(
        String errorCode,
        String message,
        ZonedDateTime timestamp,
        Map<String, String> errors,
        Map<String, Object> details,
        String traceId
) {}
