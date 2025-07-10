package com.moogsan.moongsan_backend.domain.chatting.participant.dto.query;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatMessagePageResponse {
    private List<ChatMessageResponse> chatMessageResponses;   // 채팅 메세지

    private String beforeCursorId;           // 이전 페이지용 postId

    private boolean hasBefore;               // 이전 페이지 존재 여부
}
