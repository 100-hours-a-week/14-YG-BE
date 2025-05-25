package com.moogsan.moongsan_backend.domain.groupbuy.facade.query;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.UserAccountResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.BasicList.BasicListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.WishList.WishListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipantList.ParticipantListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipatedList.ParticipatedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyUpdate.GroupBuyForUpdateResponse;
import com.moogsan.moongsan_backend.domain.user.entity.User;

import java.time.LocalDateTime;

public interface GroupBuyQueryFacade {

    // 공구 수정 화면용 단건 조회
    GroupBuyForUpdateResponse getGroupBuyEditInfo(Long postId);

    // 상세·계좌
    DetailResponse        getGroupBuyDetailInfo(Long userId, Long postId);
    UserAccountResponse   getGroupBuyHostAccountInfo(Long userId, Long postId);

    // 리스트 (커서 기반)
    PagedResponse<BasicListResponse> getGroupBuyListByCursor(
            Long userId,
            Long categoryId,
            String orderBy,
            Long cursorId,
            LocalDateTime cursorCreatedAt,
            Integer cursorPrice,
            Integer limit,
            Boolean openOnly,
            String keyword);

    PagedResponse<WishListResponse> getGroupBuyWishList(
            Long userId,
            String sort,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            Integer limit);

    PagedResponse<HostedListResponse> getGroupBuyHostedList(
            Long userId,
            String sort,
            Long cursorId,
            Integer limit);

    PagedResponse<ParticipatedListResponse> getGroupBuyParticipatedList(
            Long userId,
            String sort,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            Integer limit);

    // 참여자 목록
    ParticipantListResponse getGroupBuyParticipantsInfo(Long userId, Long postId);
}

