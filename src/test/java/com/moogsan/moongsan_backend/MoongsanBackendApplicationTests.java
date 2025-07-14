package com.moogsan.moongsan_backend;

import com.moogsan.moongsan_backend.adapters.kafka.producer.outbox.OutboxWorker;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.service.KafkaConsumerService;
import com.moogsan.moongsan_backend.support.fake.TestKafkaConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestKafkaConfiguration.class)
class MoongsanBackendApplicationTests {

	@MockBean
	KafkaConsumerService kafkaConsumerService;

	@MockBean
	OutboxWorker outboxWorker;

	@Test
	void contextLoads() {
	}

}
