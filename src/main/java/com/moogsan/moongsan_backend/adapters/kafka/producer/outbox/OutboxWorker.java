package com.moogsan.moongsan_backend.global.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxWorker {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafka;

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:5000}")
    public void pollAndPublish() {
        publishBatch();
    }

    @Transactional
    protected void publishBatch() {
        List<OutboxEventEntity> batch = outboxEventRepository.findNextBatch(
                LocalDateTime.now(),
                PageRequest.of(0, 50)
        );

        for (OutboxEventEntity e : batch) {
            try {
                kafka.send(
                        e.getKafkaTopic(),
                        e.getKafkaPartitionKey(),
                        e.getPayload()
                ).get();

                e.setStatus(OutboxEventStatus.PUBLISHED);
                e.setPublishedAt(LocalDateTime.now());
            } catch (Exception ex) {
                log.error("Outbox publish failed, id={}", e.getId(), ex);
                handleFailure(e);
            }
        }
        outboxEventRepository.saveAll(batch);
    }

    private void handleFailure(OutboxEventEntity e) {
        int retries = e.getRetryCount() + 1;
        e.setRetryCount(retries);

        if (retries >= 5) {
            e.setStatus(OutboxEventStatus.DEAD);
            log.warn("Outbox event dead-lettered, id={}", e.getId());
        } else {
            e.setStatus(OutboxEventStatus.FAILED);
            e.setNextRetryAt(LocalDateTime.now().plusSeconds((long)Math.pow(2, retries)));
        }
    }

}
