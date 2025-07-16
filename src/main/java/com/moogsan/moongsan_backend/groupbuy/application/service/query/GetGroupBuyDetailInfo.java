package com.moogsan.moongsan_backend.groupbuy.application.service.query;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.service.GenerateAliasIdService;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.groupbuy.application.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.groupbuy.domain.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.AFTER_DELETED;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class GetGroupBuyDetailInfo {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final GroupBuyQueryMapper groupBuyQueryMapper;
    private final WishRepository wishRepository;
    private final Clock now;
    private final GenerateAliasIdService generateAliasIdService;

    /// 공구 게시글 상세 조회
    public DetailResponse getGroupBuyDetailInfo(Long userId, Long postId) {

        // 유효성 검사
        GroupBuy groupBuy = fetchAndValidate(postId);

        // 공구 게시글 상세 정보 조회 및 매핑
        int aliasId = generateAliasIdService.generateAliasId(groupBuy.getId());
        boolean isHost = Objects.equals(userId, groupBuy.getUser().getId());
        boolean isParticipant = orderRepository.existsByUserIdAndGroupBuyIdAndStatusNotIn(
                userId, groupBuy.getId(), List.of("CANCELED", "REFUNDED"));
        boolean isWish = wishRepository.existsByUserIdAndGroupBuyId(userId, postId);
        return groupBuyQueryMapper.toDetailResponse(groupBuy, isHost, isParticipant, isWish, aliasId);
    }

    private GroupBuy fetchAndValidate(Long postId) {
        GroupBuy groupBuy = groupBuyRepository.findWithImagesById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        if (groupBuy.getPostStatus().equals("DELETED")) {
            throw new GroupBuyInvalidStateException(AFTER_DELETED);
        }

        return groupBuy;
    }
}
