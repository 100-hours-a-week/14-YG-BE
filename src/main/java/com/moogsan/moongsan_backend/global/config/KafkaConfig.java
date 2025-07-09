package com.moogsan.moongsan_backend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.message.SimpleMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@Configuration
@EnableKafka
@Profile("!test")
@RequiredArgsConstructor
public class KafkaConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> cf
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(cf);

        // Jackson 역직렬화
        factory.setRecordMessageConverter(new StringJsonMessageConverter(objectMapper));

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setSyncCommits(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SimpleMessage> simpleMessageListenerFactory(
            ConsumerFactory<String, SimpleMessage> cf
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, SimpleMessage>();
        factory.setConsumerFactory(cf);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setSyncCommits(true);
        return factory;
    }
}
