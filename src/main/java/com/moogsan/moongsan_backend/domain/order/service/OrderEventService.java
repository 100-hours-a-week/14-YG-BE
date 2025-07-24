package com.moogsan.moongsan_backend.domain.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.event.OrderCanceledEvent;
import com.moogsan.moongsan_backend.domain.order.mapper.OrderEventMapper;
import com.moogsan.moongsan_backend.global.infrastructure.kafka.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.mapper.GroupBuyEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.moogsan.moongsan_backend.global.infrastructure.kafka.KafkaTopics.ORDER_STATUS_CANCELED;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderEventService {

    private final OrderEventMapper eventMapper;
    private final ObjectMapper objectMapper;
    private final KafkaEventPublisher kafkaEventPublisher;

    public void publishOrderStatusCanceled(Order order, GroupBuy groupBuy) {
        int price = order.getPrice();
        int quantity = order.getQuantity();
        int totalPrice = price * quantity;
        try {
            OrderCanceledEvent eventDto =
                    eventMapper.toCanceledEvent(
                            order.getId(),
                            groupBuy.getId(),
                            groupBuy.getTitle(),
                            groupBuy.getUser().getId(),
                            order.getUser().getNickname(),
                            order.getUser().getAccountBank(),
                            order.getUser().getAccountNumber(),
                            totalPrice
                    );
            log.info("▶ orderCanceledEvent DTO = {}", eventDto);
            String payload = objectMapper.writeValueAsString(eventDto);
            kafkaEventPublisher.publish(ORDER_STATUS_CANCELED, String.valueOf(order.getId()), payload);
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize OrderCanceledEvent: orderId={}", order.getId(), e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }
}
