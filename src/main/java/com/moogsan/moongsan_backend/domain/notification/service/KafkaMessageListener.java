package com.moogsan.moongsan_backend.domain.notification.service;

import com.moogsan.moongsan_backend.domain.notification.dto.SimpleMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@EnableKafka
@Component
public class KafkaMessageListener {

    @KafkaListener(
            topics = "test.simple.message",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(SimpleMessage message, Acknowledgment ack) {
        log.info("카프카 수신 => {}", message);
        ack.acknowledge();
    }
}
