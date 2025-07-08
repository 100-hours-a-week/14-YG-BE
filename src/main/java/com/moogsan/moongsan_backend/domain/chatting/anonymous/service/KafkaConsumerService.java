package com.moogsan.moongsan_backend.domain.chatting.anonymous.service;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.dto.ChatAnonDto;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.entity.ChatAnon;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.repository.ChatAnonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final ChatAnonRepository chatAnonRepository;
    private final DeleteOldMessageService deleteOldMessages;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(topics = "chatting.anonymous.message", groupId = "chat-anon-message", containerFactory = "simpleMessageListenerFactory")
    public void consume(ChatAnonDto message, @Header(KafkaHeaders.RECEIVED_KEY) String postId, Acknowledgment ack) {
        if (postId == null) {
            System.out.println("🟡 [KafkaConsumer] Kafka 메시지 키(postId)가 null입니다.");
            return;
        }

        ChatAnon entity = message.toEntity();

        chatAnonRepository.save(entity);
        deleteOldMessages.deleteOldMessages(Long.parseLong(postId));
        simpMessagingTemplate.convertAndSend("/topic/chat-anon/" + postId, message);

        System.out.println("🟡 [KafkaConsumer] MongoDB 저장 및 WebSocket 토픽 발행 완료 - aliasId: " + message.getAliasId() + ", message: " + message.getMessage());
        ack.acknowledge();
    }
}
