package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyPickupUpdatedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.dto.GroupBuyStatusEndedEvent;
import com.moogsan.moongsan_backend.adapters.kafka.producer.mapper.GroupBuyEventMapper;
import com.moogsan.moongsan_backend.adapters.kafka.producer.publisher.KafkaEventPublisher;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.image.entity.Image;
import com.moogsan.moongsan_backend.domain.image.mapper.ImageMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.image.service.S3Service;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.GROUPBUY_PICKUP_UPDATED;
import static com.moogsan.moongsan_backend.adapters.kafka.producer.KafkaTopics.GROUPBUY_STATUS_ENDED;
import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateGroupBuy {
    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final ImageMapper imageMapper;
    private final S3Service s3Service;
    private final GroupBuyEventMapper eventMapper;
    private final ObjectMapper objectMapper;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final Clock clock;

    /// 공구 게시글 수정
    public Long updateGroupBuy(User currentUser, UpdateGroupBuyRequest updateGroupBuyRequest, Long postId) {

        log.info("▶ 전체 Request DTO = {}", updateGroupBuyRequest);
        log.info("▶ dateModificationReason = '{}'", updateGroupBuyRequest.getDateModificationReason());

        // 해당 공구가 존재하는지 조회 -> 아니면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구의 status가 open인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
                || groupBuy.getDueDate().isBefore(LocalDateTime.now(clock))) {
            throw new GroupBuyInvalidStateException(NOT_OPEN);
        }

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(currentUser.getId())) {
            throw new GroupBuyNotHostException(NOT_HOST);
        }

        // GroupBuy 기본 필드 매핑 (팩토리 메서드 사용)
        GroupBuy gb = groupBuy.updateForm(updateGroupBuyRequest);

        // 기존 S3 파일 삭제
        List<String> requested = Optional.ofNullable(updateGroupBuyRequest.getImageKeys())
                .orElseGet(Collections::emptyList);
        List<String> existing  = gb.getImages().stream()
                .map(Image::getImageKey)
                .toList();

        // 3-1. 삭제 대상: 기존에 있었지만 요청에 없는 키
        existing.stream()
                .filter(key -> !requested.contains(key))
                .forEach(key -> {
                    s3Service.deleteImage(key);
                });

        // S3 파일 이동
        String destPrefix = "group-buys";
        List<String> finalKeys = new ArrayList<>();
        for (String key : requested) {
            if (key.startsWith("group-buys/")) {
                // 이미 영구폴더에 있음 → 그대로
                finalKeys.add(key);
            } else if (key.startsWith("tmp/")) {
                String fileName = key.substring(key.lastIndexOf('/') + 1);
                String destKey  = "group-buys/" + fileName;
                s3Service.moveImage(key, destKey);
                finalKeys.add(destKey);
            } else {
                throw new IllegalArgumentException("Invalid image key: " + key);
            }
        }

        imageMapper.mapImagesToGroupBuy(finalKeys, gb);

        groupBuyRepository.save(gb);

        if (updateGroupBuyRequest.getDateModificationReason() != null) {
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
                kafkaEventPublisher.publish(GROUPBUY_PICKUP_UPDATED, String.valueOf(gb.getId()), payload);
            } catch (JsonProcessingException e) {
                log.error("❌ Failed to serialize GroupBuyPickupUpdatedEvent: groupBuyId={}", gb.getId(), e);
                throw new RuntimeException(SERIALIZATION_FAIL, e);
            }
        }

        return gb.getId();
    }
}
