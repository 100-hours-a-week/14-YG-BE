package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusEndedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.GroupBuyEventMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.GROUPBUY_STATUS_ENDED;
import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EndGroupBuy {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final Clock clock;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final GroupBuyEventMapper eventMapper;
    private final ObjectMapper objectMapper;

    /// 공구 게시글 공구 종료
    public void endGroupBuy(User currentUser, Long postId) {

        // 해당 공구가 존재하는지 조회 -> 없으면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구가 OPEN인지 조회 -> 아니면 409
        if (groupBuy.getPostStatus().equals("OPEN")) {
            throw new GroupBuyInvalidStateException(BEFORE_CLOSED);
        }

        // 해당 공구가 ENDED인지 조회 -> 맞으면 409
        if (groupBuy.getPostStatus().equals("ENDED")) {
            throw new GroupBuyInvalidStateException(AFTER_ENDED);
        }

        if (!groupBuy.isFinalized()) {
            throw new GroupBuyInvalidStateException(BEFORE_FIXED);
        }

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(currentUser.getId())) {
            throw new GroupBuyNotHostException(NOT_HOST);
        }

        //공구 게시글 status ENDED로 변경
        groupBuy.changePostStatus("ENDED");

        groupBuyRepository.save(groupBuy);

        // TODO V3에서는 참여자 채팅방 해제 카운트 시작(2주- CS 고려), 익명 채팅방 즉시 해제

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
                            participantIds,
                            groupBuy.getTitle()
                    );
            String payload = objectMapper.writeValueAsString(eventDto);
            kafkaEventPublisher.publish(GROUPBUY_STATUS_ENDED, String.valueOf(groupBuy.getId()), payload);
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize GroupBuyStatusEndedEvent: groupBuyId={}", groupBuy.getId(), e);
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }

    }
}
