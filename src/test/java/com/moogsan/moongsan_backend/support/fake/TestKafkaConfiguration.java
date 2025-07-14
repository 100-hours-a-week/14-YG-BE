package com.moogsan.moongsan_backend.support.fake;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.dto.ChatAnonDto;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import static org.mockito.Mockito.*;

@TestConfiguration
public class TestKafkaConfiguration {

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplateObject() {
        return mock(KafkaTemplate.class);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplateString() {
        return mock(KafkaTemplate.class);
    }

    @Bean
    public KafkaTemplate<String, ChatAnonDto> kafkaTemplateChatAnon() {
        return mock(KafkaTemplate.class);
    }

    @Bean(name = "simpleMessageListenerFactory")
    public KafkaListenerContainerFactory<?> simpleMessageListenerFactory() {
        return mock(ConcurrentKafkaListenerContainerFactory.class);
    }
}
