package com.moogsan.moongsan_backend.groupbuy.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.global.infrastructure.kafka.KafkaEventPublisher;
import com.moogsan.moongsan_backend.groupbuy.infrastructure.kafka.RealtimePublisher;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyPickupUpdatedEvent;
import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyStatusEndedEvent;
import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyUpdatedEvent;
import com.moogsan.moongsan_backend.groupbuy.domain.mapper.GroupBuyEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.moogsan.moongsan_backend.global.infrastructure.kafka.KafkaTopics.*;
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

    public void publishGroupBuyEnded(GroupBuy groupBuy) {
        try {
            List<Order> orders = orderRepository.findAllByGroupBuyIdOrderByStatusCustom(groupBuy.getId());

            List<Long> participantIds = orders.stream()
                    .map(order -> order.getUser().getId())
                    .distinct()
                    .toList();

            GroupBuyStatusEndedEvent eventDto =
                    eventMapper.toGroupBuyEndedEvent(
                            groupBuy.getId(),
                            groupBuy.getUser().getId(),
                            groupBuy.getTitle(),
                            participantIds
                    );
            String payload = objectMapper.writeValueAsString(eventDto);
            kafkaEventPublisher.publish(GROUPBUY_STATUS_ENDED, String.valueOf(groupBuy.getId()), payload);
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize GroupBuyStatusEndedEvent: groupBuyId={}", groupBuy.getId(), e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }

    public void publishGroupBuyUpdated(GroupBuy groupBuy) {
        GroupBuyUpdatedEvent event = GroupBuyUpdatedEvent.builder()
                .groupBuyId(groupBuy.getId())
                .build();
        realtimePublisher.publish(event);
    }
}
