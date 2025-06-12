package com.moogsan.moongsan_backend.domain.groupbuy.facade.query;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.UserAccountResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.BasicList.BasicListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipantList.ParticipantListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipatedList.ParticipatedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.WishList.WishListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyUpdate.GroupBuyForUpdateResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.*;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GroupBuyQueryFacadeImpl implements GroupBuyQueryFacade {

    private final GetGroupBuyEditInfo            editInfoSvc;
    private final GetGroupBuyDetailInfo          detailInfoSvc;
    private final GetGroupBuyHostAccountInfo     hostAccountSvc;
    private final GetGroupBuyListByCursor        listByCursorSvc;
    private final GetGroupBuyWishList            wishListSvc;
    private final GetGroupBuyHostedList          hostedListSvc;
    private final GetGroupBuyParticipatedList    participatedListSvc;
    private final GetGroupBuyParticipantsInfo    participantsInfoSvc;

    @Override
    public GroupBuyForUpdateResponse getGroupBuyEditInfo(Long userId, Long postId) {
        return editInfoSvc.getGroupBuyEditInfo(userId, postId);
    }

    @Override
    public DetailResponse getGroupBuyDetailInfo(Long userId, Long postId) {
        return detailInfoSvc.getGroupBuyDetailInfo(userId, postId);
    }

    @Override
    public UserAccountResponse getGroupBuyHostAccountInfo(Long userId, Long postId) {
        return hostAccountSvc.getGroupBuyHostAccountInfo(userId, postId);
    }

    @Override
    public PagedResponse<BasicListResponse> getGroupBuyListByCursor(
            Long userId,
            Long categoryId,
            String orderBy,
            Long cursorId,
            LocalDateTime cursorCreatedAt,
            Integer cursorPrice,
            Integer limit,
            Boolean openOnly,
            String keyword) {

        return listByCursorSvc.getGroupBuyListByCursor(
                userId, categoryId, orderBy, cursorId,
                cursorCreatedAt, cursorPrice, limit, openOnly, keyword);
    }

    @Override
    public PagedResponse<WishListResponse> getGroupBuyWishList(
            Long userId,
            String sort,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            Integer limit) {

        return wishListSvc.getGroupBuyWishList(userId, sort, cursorCreatedAt, cursorId, limit);
    }

    @Override
    public PagedResponse<HostedListResponse> getGroupBuyHostedList(
            Long userId,
            String sort,
            Long cursorId,
            Integer limit) {

        return hostedListSvc.getGroupBuyHostedList(userId, sort, cursorId, limit);
    }

    @Override
    public PagedResponse<ParticipatedListResponse> getGroupBuyParticipatedList(
            Long userId,
            String sort,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            Integer limit) {

        return participatedListSvc.getGroupBuyParticipatedList(
                userId, sort, cursorCreatedAt, cursorId, limit);
    }

    @Override
    public ParticipantListResponse getGroupBuyParticipantsInfo(Long userId, Long postId) {
        return participantsInfoSvc.getGroupBuyParticipantsInfo(userId, postId);
    }
}
