package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusEndedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusFinalizedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.GroupBuyEventMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.GROUPBUY_STATUS_ENDED;
import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.GROUPBUY_STATUS_FINALIZED;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FinalizeGroupBuy {

    private final OrderRepository orderRepository;
    private final KafkaEventPublisher publisher;
    private final GroupBuyEventMapper mapper;
    private final ObjectMapper objectMapper;

    public void finalizeGroupBuy(GroupBuy groupBuy) {

        // 이미 확정이면 바로 반환 (중복차단)
        if (groupBuy.isFixed()) return;

        // 유효 주문 수 & 미확정 주문 수 한 번에 구하기
        long totalValid   = orderRepository.countByGroupBuyIdAndStatusNotIn(
                groupBuy.getId(),
                List.of("CANCELED", "REFUNDED"));
        long unconfirmed  = orderRepository.countByGroupBuyIdAndStatusNot(
                groupBuy.getId(),
                "CONFIRMED");

        // 확정 판정
        if (totalValid > 0 && unconfirmed == 0) {
            groupBuy.setFixed(true);          // 플래그 갱신

            publishFinalizedEvent(groupBuy);  // 오직 '이번'에만 호출
        }
    }

    private void publishFinalizedEvent(GroupBuy groupBuy) {
        try {
            // 참여자 ID 모으기 — 필요할 때만 SELECT
            List<Order> orders = orderRepository.findAllByGroupBuyIdOrderByStatusCustom(groupBuy.getId());

            List<Long> participantIds = orders.stream()
                    .map(order -> order.getUser().getId())
                    .distinct()
                    .toList();

            GroupBuyStatusFinalizedEvent dto =
                    mapper.toGroupBuyFinalizedEvent(
                            groupBuy.getId(),
                            groupBuy.getUser().getId(),
                            participantIds,
                            groupBuy.getTitle(),
                            String.valueOf(groupBuy.getParticipantCount()),
                            String.valueOf(groupBuy.getTotalAmount())
                    );

            publisher.publish(
                    GROUPBUY_STATUS_FINALIZED,
                    String.valueOf(groupBuy.getId()),
                    objectMapper.writeValueAsString(dto)
            );
            log.info("✅ GroupBuy finalized: id={}", groupBuy.getId());

        } catch (JsonProcessingException e) {
            log.error("❌ 역직렬화 실패 (GroupBuyStatusFinalizedEvent)", e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }
}
