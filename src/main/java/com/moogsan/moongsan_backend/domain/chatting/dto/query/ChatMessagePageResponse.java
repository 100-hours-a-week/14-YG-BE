package com.moogsan.moongsan_backend.domain.chatting.dto.query;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatMessagePageResponse {
    private List<ChatMessageResponse> chatMessageResponses;   // 채팅 메세지

    private String nextCursorId;           // 다음 페이지용 postId
    private LocalDateTime nextCreatedAt;  // 다음 페이지용 createdAt

    private boolean hasNext;              // 다음 페이지 존재 여부
}
