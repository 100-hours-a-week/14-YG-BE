package com.moogsan.moongsan_backend.domain.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderCanceledEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderConfirmedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.OrderPendingEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.OrderEventMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.dto.request.OrderStatusUpdateRequest;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;
import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.*;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusUpdateService {
    private final OrderRepository orderRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final OrderEventMapper eventMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void updateOrderStatuses(List<OrderStatusUpdateRequest> requests) {
        for (OrderStatusUpdateRequest request : requests) {
            Order order = orderRepository.findById(request.getOrderId()
            ).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));

            order.updateStatus(request.getStatus());

            String currentStatus = request.getStatus();

            Object eventDto;
            String topic;

            GroupBuy groupBuy = order.getGroupBuy();

            switch (currentStatus) {
                case "CONFIRMED":
                    eventDto = eventMapper.toConfirmedEvent(
                            order.getId(),
                            groupBuy.getId(),
                            order.getUser().getId(),
                            order.getUser().getNickname(),
                            groupBuy.getTitle()
                    );
                    topic = ORDER_STATUS_CONFIRMED;
                    break;
                case "REFUNDED":
                    eventDto = eventMapper.toRefundedEvent(
                            order.getId(),
                            groupBuy.getId(),
                            order.getUser().getId(),
                            order.getUser().getNickname(),
                            groupBuy.getTitle()
                    );
                    topic = ORDER_STATUS_REFUNDED;
                    break;
                default:
                    log.warn("알 수 없는 주문 상태, 이벤트 미전송: {}", currentStatus);
                    return;
            }

            try {
                String payload = objectMapper.writeValueAsString(eventDto);
                kafkaEventPublisher.publish(
                        topic,
                        String.valueOf(order.getId()),
                        payload
                );
                log.info("✅ Kafka event sent: topic={}, orderId={}", topic, order.getId());
            } catch (JsonProcessingException e) {
                log.error("❌ Failed to serialize event for topic={}", topic, e);
                throw new RuntimeException(SERIALIZATION_FAIL, e);
            }
        }
    }
}
