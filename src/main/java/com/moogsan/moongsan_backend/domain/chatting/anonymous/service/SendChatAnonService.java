package com.moogsan.moongsan_backend.domain.chatting.anonymous.service;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.dto.ChatAnonDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SendChatAnonService {

    private final KafkaProducerService kafkaProducerService;

    // 수신된 메시지를 처리하고 Kafka 전송 및 브로드캐스트하는 메서드
    public void processMessage(ChatAnonDto message) {
        // 필수 값 확인
        if (message.getMessage() == null) {
            throw new IllegalArgumentException("message는 필수 입력값입니다.");
        }

        // createdAt이 비어있으면 현재 시간으로 설정
        if (message.getCreatedAt() == null) {
            LocalDateTime now = LocalDateTime.now();
            message.setCreatedAt(now);
        }

        // Kafka로 메시지 발행
        kafkaProducerService.send(message);
    }
}
