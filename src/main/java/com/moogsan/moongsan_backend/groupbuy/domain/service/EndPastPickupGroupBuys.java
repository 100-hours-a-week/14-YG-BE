package com.moogsan.moongsan_backend.groupbuy.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.global.realtime.GroupBuyRealtimeNotifier;
import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyStatusEndedEvent;
import com.moogsan.moongsan_backend.groupbuy.domain.mapper.GroupBuyEventMapper;
import com.moogsan.moongsan_backend.global.infrastructure.kafka.KafkaEventPublisher;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.global.infrastructure.kafka.KafkaTopics.GROUPBUY_STATUS_ENDED;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EndPastPickupGroupBuys {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final GroupBuyEventMapper eventMapper;
    private final ObjectMapper objectMapper;
    private final GroupBuyRealtimeNotifier groupBuyRealtimeNotifier;

    /// 공구 종료 (백그라운드 API)
    public void endPastPickupGroupBuys(LocalDateTime now) {
        List<GroupBuy> toEnd = groupBuyRepository
                .findByPostStatusAndPickupDateLessThanEqual("CLOSED", now);

        for (GroupBuy gb : toEnd) {
            gb.changePostStatus("ENDED");

            // 공구 상태 업데이트 이벤트 발행
            groupBuyRealtimeNotifier.publishUpdated(gb);

            try {
                List<Order> orders = orderRepository.findAllByGroupBuyIdOrderByStatusCustom(gb.getId());

                List<Long> participantIds = orders.stream()
                        .map(order -> order.getUser().getId())
                        .distinct()
                        .toList();

                GroupBuyStatusEndedEvent eventDto =
                        eventMapper.toGroupBuyEndedEvent(
                                gb.getId(),
                                gb.getUser().getId(),
                                gb.getTitle(),
                                participantIds
                        );
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
