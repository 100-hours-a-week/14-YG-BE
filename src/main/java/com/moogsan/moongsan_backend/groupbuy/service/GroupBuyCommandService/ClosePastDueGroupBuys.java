package com.moogsan.moongsan_backend.groupbuy.service.GroupBuyCommandService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuy.GroupBuyStatusClosedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuy.GroupBuyUpdatedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.GroupBuyEventMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.groupbuy.service.GroupBuySseService.publisher.RealtimePublisher;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.GROUPBUY_STATUS_CLOSED;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClosePastDueGroupBuys {

    private final GroupBuyRepository groupBuyRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final GroupBuyEventMapper eventMapper;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final RealtimePublisher realtimePublisher;

    ///  공구 모집 마감(백그라운드 API)
    public void closePastDueGroupBuys(LocalDateTime now) {
        List<GroupBuy> expired = groupBuyRepository
                .findByPostStatusAndDueDateBefore("OPEN", now);

        for (GroupBuy gb : expired) {
            gb.changePostStatus("CLOSED");

            // 공구 상태 업데이트 이벤트 발행
            GroupBuyUpdatedEvent event = GroupBuyUpdatedEvent.builder()
                    .groupBuyId(gb.getId())
                    .build();
            realtimePublisher.publish(event);

            List<Order> orders = orderRepository.findAllByGroupBuyIdOrderByStatusCustom(gb.getId());

            List<Long> participantIds = orders.stream()
                    .map(order -> order.getUser().getId())
                    .distinct()
                    .toList();

            try {
                GroupBuyStatusClosedEvent eventDto =
                        eventMapper.toGroupBuyClosedEvent(
                                gb.getId(),
                                gb.getUser().getId(),
                                participantIds,
                                gb.getTitle(),
                                String.valueOf(gb.getParticipantCount()),
                                String.valueOf(gb.getTotalAmount())
                        );
                String payload = objectMapper.writeValueAsString(eventDto);
                kafkaEventPublisher.publish(GROUPBUY_STATUS_CLOSED, String.valueOf(gb.getId()), payload);
            } catch (JsonProcessingException e) {
                log.error("❌ Failed to serialize GroupBuyStatusClosedEvent: groupBuyId={}", gb.getId(), e);
                throw new RuntimeException(SERIALIZATION_FAIL, e);
            }
        }
        groupBuyRepository.saveAll(expired);
    }
}
