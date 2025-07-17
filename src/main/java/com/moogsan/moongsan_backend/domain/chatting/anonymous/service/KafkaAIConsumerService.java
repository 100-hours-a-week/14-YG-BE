package com.moogsan.moongsan_backend.domain.chatting.anonymous.service;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.dto.ChatAnonDto;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.repository.ChatAnonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaAIConsumerService {

    private final ChatAnonRepository chatAnonRepository;

    @KafkaListener(
            topics = "chat.anon.moderation.created",
            groupId = "chat-anon-moderation",
            containerFactory = "chatAnonMessageListenerFactory")
    public void consumeModeration(ChatAnonDto message, Acknowledgment ack) {
        try {
            chatAnonRepository.findById(message.getMessageId())
                .ifPresentOrElse(
                    existingMessage -> {
                        existingMessage.setPostId(message.getPostId());
                        existingMessage.setParticipantId(message.getParticipantId());
                        existingMessage.setMessageContent(message.getMessageContent());
                        existingMessage.setIsSafe(message.isSafe());
                        existingMessage.setBlurReason(message.getBlurReason());
                        existingMessage.setCreatedAt(message.getCreatedAt());
                        chatAnonRepository.save(existingMessage);
                        System.out.println("✅ 전체 메시지 덮어쓰기 완료: " + message.getMessageId());
                    },
                    () -> System.out.println("⚠️ 해당 messageId 없음. 무시됨: " + message.getMessageId())
                );
            ack.acknowledge();
        } catch (Exception e) {
            System.err.println("❌ Moderation 메시지 처리 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
