package com.moogsan.moongsan_backend.participantchat.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 토픽: chat.part.message.created
 * 설명: 채팅 배치 작업용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatMessagePersistEvent {
    private Long chatRoomId;
    private String chatMessageId;
}
