package com.moogsan.moongsan_backend.global.config;

import com.moogsan.moongsan_backend.domain.notification.dto.SimpleMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SimpleMessage> kafkaListenerContainerFactory(
            ConsumerFactory<String, SimpleMessage> cf
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, SimpleMessage>();
        factory.setConsumerFactory(cf);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setSyncCommits(true);

        // 재시도 추가 예정
        return factory;
    }
}
