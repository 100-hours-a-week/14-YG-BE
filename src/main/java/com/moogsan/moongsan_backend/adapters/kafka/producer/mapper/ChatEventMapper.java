package com.moogsan.moongsan_backend.adapters.kafka.producer.mapper;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusClosedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.ParticipantChatMessageCreatedEvent;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ChatEventMapper {

    // 참여자 채팅 작성 이벤트
    public ParticipantChatMessageCreatedEvent toParticipantChatMessageCreatedEvent(ChatRoom chatRoom, ChatMessageDocument chatMessageDocument) {
        return ParticipantChatMessageCreatedEvent.builder()
                .chatRoomId(chatRoom.getId())
                .chatMessageId(chatMessageDocument.getId())
                .authorId(chatMessageDocument.getChatParticipantId())
                .occurredAt(Instant.now().toString())
                .build();
    }
}
