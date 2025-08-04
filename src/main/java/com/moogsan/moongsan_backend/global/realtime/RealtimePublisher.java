package com.moogsan.moongsan_backend.global.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimePublisher {
    private final RealtimeBroadcaster broadcaster;
    private final ObjectMapper objectMapper;

    private static final String TYPE = "GROUPBUY_UPDATED";
    private static final String TOPIC_PREFIX = "groupbuy:";

    public void publish(GroupBuyUpdatedEvent event) {
        try {
            var payload = GroupBuyRealtimePayload.builder()
                    .groupBuyId(event.getGroupBuyId())
                    .soldAmount(event.getSoldAmount())
                    .leftAmount(event.getLeftAmount())
                    .build();

            long version = System.currentTimeMillis(); // 임시 단조 증가 대용

            String topic = TOPIC_PREFIX + event.getGroupBuyId();

            // (선택) 디버깅
            log.debug("📡 {} topic={} v={} payload={}",
                    TYPE, topic, version, objectMapper.writeValueAsString(payload));

            broadcaster.publish(topic, TYPE, version, payload);

        } catch (Exception e) {
            throw new RuntimeException("Realtime publish failed", e);
        }
    }
}
