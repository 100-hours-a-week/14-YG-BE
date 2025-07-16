package com.moogsan.moongsan_backend.groupbuy.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.global.infrastructure.kafka.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.global.infrastructure.kafka.publisher.RealtimePublisher;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyPickupUpdatedEvent;
import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyUpdatedEvent;
import com.moogsan.moongsan_backend.groupbuy.domain.mapper.GroupBuyEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.moogsan.moongsan_backend.global.infrastructure.kafka.KafkaTopics.GROUPBUY_PICKUP_UPDATED;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupBuyEventService {

    private final GroupBuyEventMapper eventMapper;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final RealtimePublisher realtimePublisher;

    public void publishPickupUpdated(GroupBuy groupBuy) {
        // 공구 상태 업데이트 이벤트 발행
        GroupBuyUpdatedEvent event = GroupBuyUpdatedEvent.builder()
                .groupBuyId(groupBuy.getId())
                .build();
        realtimePublisher.publish(event);

        List<Order> orders = orderRepository.findAllByGroupBuyIdOrderByStatusCustom(groupBuy.getId());

        List<Long> participantIds = orders.stream()
                .map(order -> order.getUser().getId())
                .distinct()
                .toList();

        try {
            GroupBuyPickupUpdatedEvent eventDto =
                    eventMapper.toGroupBuyPickupUpdatedEvent(
                            groupBuy.getId(),
                            participantIds,
                            groupBuy.getTitle(),
                            String.valueOf(groupBuy.getPickupDate()),
                            groupBuy.getDateModificationReason()
                    );
            String payload = objectMapper.writeValueAsString(eventDto);
            kafkaEventPublisher.publish(GROUPBUY_PICKUP_UPDATED, String.valueOf(groupBuy.getId()), payload);
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize GroupBuyPickupUpdatedEvent: groupBuyId={}", groupBuy.getId(), e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }

    }
}
