package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusClosedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusEndedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.GroupBuyEventMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.GROUPBUY_STATUS_ENDED;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EndPastPickupGroupBuys {

    private final GroupBuyRepository groupBuyRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final GroupBuyEventMapper eventMapper;
    private final ObjectMapper objectMapper;

    /// 공구 종료 (백그라운드 API)
    public void endPastPickupGroupBuys(LocalDateTime now) {
        List<GroupBuy> toEnd = groupBuyRepository
                .findByPostStatusAndPickupDateLessThanEqual("CLOSED", now);

        for (GroupBuy gb : toEnd) {
            gb.changePostStatus("ENDED");
            try {
                GroupBuyStatusEndedEvent eventDto =
                        eventMapper.toGroupBuyEndedEvent(gb, "ENDED");
                String payload = objectMapper.writeValueAsString(eventDto);
                kafkaEventPublisher.publish(GROUPBUY_STATUS_ENDED, String.valueOf(gb.getId()), payload);
            } catch (JsonProcessingException e) {
                log.error("❌ Failed to serialize GroupBuyStatusEndedEvent: groupBuyId={}", gb.getId(), e);
                throw new RuntimeException(SERIALIZATION_FAIL, e);
            }
        }
        groupBuyRepository.saveAll(toEnd);
    }
}
