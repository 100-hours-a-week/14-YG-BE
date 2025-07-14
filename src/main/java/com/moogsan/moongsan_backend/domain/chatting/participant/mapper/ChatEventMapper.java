package com.moogsan.moongsan_backend.domain.chatting.participant.mapper;

import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.Chat.ChatMessagePersistEvent;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatMessageDocument;

public class ChatEventMapper {
    public ChatMessagePersistEvent toChatMessagePersistEvent(Long id, ChatMessageDocument document) {
        return null;
    }
}
