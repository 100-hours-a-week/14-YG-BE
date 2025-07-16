package com.moogsan.moongsan_backend.groupbuy.application.service.query;

import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.ParticipantList.ParticipantListResponse;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.ParticipantList.ParticipantResponse;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.groupbuy.application.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.groupbuy.domain.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.NOT_HOST;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class GetGroupBuyParticipantsInfo {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final GroupBuyQueryMapper groupBuyQueryMapper;

    /// 공구 참여자 조회
    public ParticipantListResponse getGroupBuyParticipantsInfo(Long userId, Long postId) {

        // 해당 공구가 존재하는지 조회 -> 없으면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(userId)) {
            throw new GroupBuyNotHostException(NOT_HOST);
        }

        // 주문 리스트 조회
        List<Order> orders = orderRepository.findByGroupBuyIdAndStatusNot(postId, "CANCELED");

        // DTO 매핑
        List<ParticipantResponse> participantList = orders.stream()
                .map(groupBuyQueryMapper::toParticipantResponse)
                .toList();

        return ParticipantListResponse.builder()
                .participants(participantList)
                .build();
    }
}
