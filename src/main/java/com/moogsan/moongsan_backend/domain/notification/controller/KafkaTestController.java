package com.moogsan.moongsan_backend.domain.notification.controller;

import com.moogsan.moongsan_backend.domain.notification.dto.SimpleMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kafka")
public class KafkaTestController {

    private final KafkaTemplate<String, SimpleMessage> kafkaTemplate;

    @PostMapping("/send")
    public void send(@RequestBody SimpleMessage message) {
        log.info("[KAFKA SEND 요청] message={}", message);
        try {
            SendResult<String,SimpleMessage> r = kafkaTemplate
                    .send("test.simple.message", message.getContent(), message)
                    .get(10, TimeUnit.SECONDS);
            log.info("✅ 전송 동기 확인 offset={}", r.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("❌ 전송 동기 실패", e);
        }
    }
}
