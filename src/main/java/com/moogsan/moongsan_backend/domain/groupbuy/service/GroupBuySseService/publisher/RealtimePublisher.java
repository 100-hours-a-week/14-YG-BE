package com.moogsan.moongsan_backend.domain.groupbuy.service.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuy.GroupBuyUpdatedEvent;
import com.moogsan.moongsan_backend.adapters.sse.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimePublisher {
    private final SseEmitterRepository emitterRepository;
    private final ObjectMapper objectMapper;

    public void publish(GroupBuyUpdatedEvent event) {
        try {
            // 필요한 경우 디버그용 JSON 직렬화
            String json = objectMapper.writeValueAsString(event);

            emitterRepository.send(
                    event.getGroupBuyId().toString(),
                    event.getGroupBuyId().toString()
            );

            log.debug("📡 SSE Broadcast → {}", event.getGroupBuyId());

        } catch (Exception e) {
            throw new RuntimeException("Realtime publish failed", e);
        }
    }
}
