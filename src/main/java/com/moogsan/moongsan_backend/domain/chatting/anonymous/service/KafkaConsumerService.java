package com.moogsan.moongsan_backend.domain.chatting.anonymous.service;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.dto.ChatAnonDto;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.entity.ChatAnon;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.repository.ChatAnonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final ChatAnonRepository chatAnonRepository;
    private final DeleteOldMessageService deleteOldMessages;

    @KafkaListener(topics = "chatting.anonymous.message", groupId = "${KAFKA_CONSUMER_GROUP}", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ChatAnonDto message, @Header(KafkaHeaders.RECEIVED_KEY) String postId, Acknowledgment ack) {
        if (postId == null) {
            System.out.println("🟡 [KafkaConsumer] Kafka 메시지 키(postId)가 null입니다.");
            return;
        }

        ChatAnon entity = message.toEntity();

        chatAnonRepository.save(entity);
        deleteOldMessages.deleteOldMessages(Long.parseLong(postId));

        System.out.println("🟡 [KafkaConsumer] MongoDB 저장 완료 - aliasId: " + message.getAliasId() + ", message: " + message.getMessage());
        ack.acknowledge();;
    }
}
