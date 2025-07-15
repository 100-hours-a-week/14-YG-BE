package com.moogsan.moongsan_backend.groupbuy.domain.dto;

import lombok.Builder;

@Builder
public class ChatBotRequest {
    private String message;
    private String sessionId;
    private Long userId;
    private String userName;
}
