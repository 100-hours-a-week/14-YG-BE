package com.moogsan.moongsan_backend.adapters.kafka.producer.mapper;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.Chat.ChatMessagePersistEvent;
import org.springframework.stereotype.Component;

@Component
public class ChatEventMapper {
    public ChatMessagePersistEvent toChatMessagePersistEvent(
            Long chatRoomId, String chatMessageId
    ){
        return ChatMessagePersistEvent.builder()
                .chatRoomId(chatRoomId)
                .chatMessageId(chatMessageId)
                .build();
    }
}
