package com.moogsan.moongsan_backend.domain.chatting.anonymous.service;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.dto.ChatAnonDto;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.entity.ChatAnon;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.repository.ChatAnonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendChatAnonService {

    private final KafkaProducerService kafkaProducerService;

    // 수신된 메시지를 처리하고 Kafka 전송 및 브로드캐스트하는 메서드
    public void processMessage(ChatAnonDto message) {
        // 필수 값 확인
        if (message.getMessageContent() == null) {
            throw new IllegalArgumentException("messageContent는 필수 입력값입니다.");
        }

        // messageId가 비어 있으면 UUID로 생성
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }

        // createdAt이 비어있으면 현재 시간으로 설정
        if (message.getCreatedAt() == null) {
            LocalDateTime now = LocalDateTime.now();
            message.setCreatedAt(now);
        }

        // type이 비어 있으면 "Normal"로 설정
        if (message.getType() == null) {
            message.setType("Normal");
        }

        // Kafka로 메시지 발행
        kafkaProducerService.send(message);
    }
}
