package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 토픽: chat.part.message.created
 * 설명: 참여자 채팅 메세지 생성 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ParticipantChatMessageCreatedEvent extends BaseEvent{
    private Long chatRoomId;        // 채팅방 아이디
    private String chatMessageId;     // 채팅 아이디
    private Long authorId;          // 작성자 아이디
}
