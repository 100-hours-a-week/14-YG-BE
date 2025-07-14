package com.moogsan.moongsan_backend.domain.chatting.participant.dto.query;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    // 식별/메타
    private String messageId;            // 채팅 메세지 아이디

    // 본문
    private Long participantId;        // 참여자 아이디
    private String nickname;           // 참여자 닉네임
    private String profileImageKey;    // 참여자 프로필 이미지
    private String messageContent;     // 가장 최근 메세지 내용
    private LocalDateTime createdAt;   // 작성 시각
}
