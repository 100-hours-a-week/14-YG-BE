package com.moogsan.moongsan_backend.participantchat.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.global.infrastructure.kafka.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatRoom;
import com.moogsan.moongsan_backend.participantchat.domain.event.ChatMessagePersistEvent;
import com.moogsan.moongsan_backend.participantchat.domain.mapper.ChatEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.moogsan.moongsan_backend.global.infrastructure.kafka.KafkaTopics.CHAT_PART_MESSAGE_CREATED;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantChatEventService {

    private final KafkaEventPublisher kafkaEventPublisher;
    private final ChatEventMapper eventMapper;
    private final ObjectMapper objectMapper;

    public void publishChatPersist(ChatRoom chatRoom, ChatMessageDocument document) {
        try {
            ChatMessagePersistEvent eventDto =
                    eventMapper.toChatMessagePersistEvent(chatRoom.getId(), document.getId());
            String payload = objectMapper.writeValueAsString(eventDto);
            kafkaEventPublisher.publish(CHAT_PART_MESSAGE_CREATED, String.valueOf(document.getId()), payload);
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize ChatMessagePersistEvent: documentId={}", document.getId(), e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }
}
