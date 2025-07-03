package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyDueApproachingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.GroupBuyEventMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.GROUPBUY_DUE_APPROACHING;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PublishDueApproachingEvents {
    private final GroupBuyRepository groupBuyRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final GroupBuyEventMapper eventMapper;
    private final ObjectMapper objectMapper;

    /// 공구 모집 종료 하루 전 (백그라운드 API)
    public void publishDueApproachingEvents(LocalDateTime tomorrow) {
        List<GroupBuy> toEnd = groupBuyRepository
                .findByPostStatusAndPickupDateLessThanEqual("OPEN", tomorrow);

        for (GroupBuy gb : toEnd) {
            try {
                GroupBuyDueApproachingEvent eventDto =
                        eventMapper.toGroupBuyDueApproachingEvent(gb);
                String payload = objectMapper.writeValueAsString(eventDto);
                kafkaTemplate.send(GROUPBUY_DUE_APPROACHING, String.valueOf(gb.getId()), payload);
            } catch (JsonProcessingException e) {
                log.error("❌ Failed to serialize GroupBuyDueApproachingEvent: groupBuyId={}", gb.getId(), e);
                throw new RuntimeException(SERIALIZATION_FAIL, e);
            }
        }
    }
}
