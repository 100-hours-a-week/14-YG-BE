package com.moogsan.moongsan_backend.groupbuy.domain.dto;

public record ChatSseResponse (
        String type,       // "processing" | "ai_response" | "completion"
        String content,    // 본문
        String agent,      // "chat" | "search" | "create"
        String timestamp
){}
