package com.moogsan.moongsan_backend.groupbuy.service.GroupBuyQueryService;

import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyDetail.UserAccountResponse;
import com.moogsan.moongsan_backend.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.groupbuy.exception.specific.GroupBuyNotParticipantException;
import com.moogsan.moongsan_backend.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class GetGroupBuyHostAccountInfo {
    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final GroupBuyQueryMapper groupBuyQueryMapper;

    /// 주최자 계좌 정보 조회
    public UserAccountResponse getGroupBuyHostAccountInfo(Long userId, Long postId) {

        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 만약 요청한 유저가 해당 공구의 주최자라면 주문 여부 검사 없이 바로 리턴
        if (groupBuy.getUser().getId().equals(userId)) {
            return groupBuyQueryMapper.toHostAccount(groupBuy);
        }

        // 해당 공구의 주문 테이블에 해당 유저의 주문이 존재하는지 조회 -> 아니면 404
        Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(userId, groupBuy.getId(), "CANCELED")
                .orElseThrow(GroupBuyNotParticipantException::new);

        return groupBuyQueryMapper.toHostAccount(groupBuy);
    }
}
