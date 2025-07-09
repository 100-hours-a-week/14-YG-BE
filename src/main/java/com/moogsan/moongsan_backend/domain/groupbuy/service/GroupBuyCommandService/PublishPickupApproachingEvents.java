package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyPickupApproachingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.GroupBuyEventMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.GROUPBUY_PICKUP_APPROACHING;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PublishPickupApproachingEvents {
    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final GroupBuyEventMapper eventMapper;
    private final ObjectMapper objectMapper;

    /// 공구 종료 하루 전 (백그라운드 API)
    public void publishPickupApproachingEvents(LocalDateTime tomorrow) {
        List<GroupBuy> toEnd = groupBuyRepository
                .findByPostStatusAndPickupDateLessThanEqual("OPEN", tomorrow);

        for (GroupBuy gb : toEnd) {
            try {
                List<Order> orders = orderRepository.findAllByGroupBuyIdOrderByStatusCustom(gb.getId());

                List<Long> participantIds = orders.stream()
                        .map(order -> order.getUser().getId())
                        .distinct()
                        .toList();

                GroupBuyPickupApproachingEvent eventDto =
                        eventMapper.toGroupBuyPickupApproachingEvent(
                                gb.getId(),
                                gb.getUser().getId(),
                                gb.getTitle(),
                                participantIds,
                                String.valueOf(gb.getParticipantCount()),
                                String.valueOf(gb.getTotalAmount())
                        );
                String payload = objectMapper.writeValueAsString(eventDto);
                kafkaEventPublisher.publish(GROUPBUY_PICKUP_APPROACHING, String.valueOf(gb.getId()), payload);
            } catch (JsonProcessingException e) {
                log.error("❌ Failed to serialize GroupBuyPickupApproachingEvent: groupBuyId={}", gb.getId(), e);
                throw new RuntimeException(SERIALIZATION_FAIL, e);
            }
        }
    }
}
