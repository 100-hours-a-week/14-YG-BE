package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class GetGroupBuyDetailInfo {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final GroupBuyQueryMapper groupBuyQueryMapper;
    private final WishRepository wishRepository;

    /// 공구 게시글 상세 조회
    public DetailResponse getGroupBuyDetailInfo(Long userId, Long postId) {

        GroupBuy groupBuy = groupBuyRepository.findWithImagesById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        //log.info("Checking participant: userId={}, postId={}", userId, postId);
        boolean isParticipant = orderRepository.existsParticipant(userId, postId, "CANCELED");
        boolean isWish = wishRepository.existsByUserIdAndGroupBuyId(userId, postId);

        return groupBuyQueryMapper.toDetailResponse(groupBuy, isParticipant, isWish);
    }
}
