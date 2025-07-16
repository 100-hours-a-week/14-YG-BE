package com.moogsan.moongsan_backend.domain.chatting.anonymous.service;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.dto.ChatAnonDto;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.entity.ChatAnon;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.repository.ChatAnonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final ChatAnonRepository chatAnonRepository;
    private final DeleteOldMessageService deleteOldMessages;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(topics = "chat.anon.message.created", groupId = "chat-anon-message", containerFactory = "chatAnonMessageListenerFactory")
    public void consume(ChatAnonDto message, Acknowledgment ack) {
        try {
            ChatAnon entity = message.toEntity();

            chatAnonRepository.save(entity);
            deleteOldMessages.deleteOldMessages(message.getPostId());
            simpMessagingTemplate.convertAndSend("/topic/chat-anon/" + message.getPostId(), message);

            System.out.println("🟡 [KafkaConsumer] MongoDB 저장 및 WebSocket 토픽 발행 완료 -\n" +
                    "  messageId=" + message.getMessageId() + "\n" +
                    "  postId=" + message.getPostId() + "\n" +
                    "  participantId=" + message.getParticipantId() + "\n" +
                    "  messageContent=" + message.getMessageContent() + "\n" +
                    "  type=" + message.getType() + "\n" +
                    "  createdAt=" + message.getCreatedAt());
            ack.acknowledge();
        } catch (Exception e) {
            System.err.println("Kafka 메시지 역직렬화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
