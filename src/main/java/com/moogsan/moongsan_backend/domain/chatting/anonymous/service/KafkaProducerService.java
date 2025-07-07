package com.moogsan.moongsan_backend.domain.chatting.anonymous.service;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.dto.ChatAnonDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, ChatAnonDto> kafkaTemplate;
    private static final String TOPIC = "chatting.anonymous.message";

    public void send(ChatAnonDto message){
        kafkaTemplate.send(TOPIC, String.valueOf(message.getPostId()), message);
        System.out.println("🟡 [KafkaProducer] Kafka 메시지 발행: postId=" + message.getPostId()
                           + ", aliasId=" + message.getAliasId()
                           + ", message=" + message.getMessage());
    }
}
