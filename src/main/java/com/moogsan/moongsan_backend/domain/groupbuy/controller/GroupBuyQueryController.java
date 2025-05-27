package com.moogsan.moongsan_backend.domain.groupbuy.controller;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.UserAccountResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.BasicList.BasicListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipantList.ParticipantListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipatedList.ParticipatedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.WishList.WishListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyUpdate.GroupBuyForUpdateResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.query.GroupBuyQueryFacade;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.*;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys")
public class GroupBuyQueryController {

    private final GroupBuyQueryFacade queryFacade;

    /// 공구 게시글 수정 전 정보 조회 SUCCESS
    @GetMapping("/{postId}/edit")
    public ResponseEntity<WrapperResponse<GroupBuyForUpdateResponse>> getGroupBuyEditInfo(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        if (userDetails == null) {
            throw new UnauthenticatedAccessException("로그인이 필요합니다.");
        }

        GroupBuyForUpdateResponse groupBuyForUpdate = queryFacade.getGroupBuyEditInfo(postId);
        return ResponseEntity.ok(
                WrapperResponse.<GroupBuyForUpdateResponse>builder()
                        .message("공구 게시글 수정용 정보를 성공적으로 조회했습니다.")
                        .data(groupBuyForUpdate)
                        .build()
        );
    }

    /// 공구 게시글 상세 조회 V2 update - wish SUCCESS
    @GetMapping("/{postId}")
    public ResponseEntity<WrapperResponse<DetailResponse>> getGroupBuyDetailInfo(
        @PathVariable Long postId,
        @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = (userDetails != null) ? userDetails.getUser().getId() : null;

        DetailResponse detail = queryFacade.getGroupBuyDetailInfo(userId, postId);
        return ResponseEntity.ok(
                WrapperResponse.<DetailResponse>builder()
                        .message("공구 게시글 상세 정보를 성공적으로 조회했습니다.")
                        .data(detail)
                        .build()
        );
    }

    ///  주최자 계좌 정보 조회 V2 update
    @GetMapping("/{postId}/host/account")
    public ResponseEntity<WrapperResponse<UserAccountResponse>> getGroupBuyHostAccountInfo(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthenticatedAccessException("로그인이 필요합니다.");
        }

        UserAccountResponse accountResponse = queryFacade.getGroupBuyHostAccountInfo(
                userDetails.getUser().getId(), postId
        );
        return ResponseEntity.ok(
                WrapperResponse.<UserAccountResponse>builder()
                        .message("공구 게시글 주최자 계좌 정보를 성공적으로 조회했습니다.")
                        .data(accountResponse)
                        .build()
        );
    }

    /// 공구 리스트 조회  V2 update - wish SUCCESS
    @GetMapping
    public ResponseEntity<WrapperResponse<PagedResponse<BasicListResponse>>> getGroupBuyListByCursor(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "orderBy", defaultValue = "created") String orderBy,
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            // 커서 페이징용 추가 파라미터들
            @RequestParam(value = "cursorCreatedAt", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime cursorCreatedAt,
            @RequestParam(value = "cursorPrice", required = false) Integer cursorPrice,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "openOnly", required = false) Boolean openOnly,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Long userId = (principal != null) ? principal.getUser().getId() : null;

        PagedResponse<BasicListResponse> pagedResponse =
                queryFacade.getGroupBuyListByCursor(userId, categoryId, orderBy,
                        cursorId, cursorCreatedAt, cursorPrice, limit, openOnly, keyword);
        return ResponseEntity.ok(
                WrapperResponse.<PagedResponse<BasicListResponse>>builder()
                        .message("전체 공구 리스트를 성공적으로 조회했습니다.")
                        .data(pagedResponse)
                        .build()
        );
    }


    /// 관심 공구 리스트 조회 SUCCESS, 커서 적용 완료
    @GetMapping("/users/me/wishes")
    public ResponseEntity<WrapperResponse<PagedResponse<WishListResponse>>> getGroupBuyWishList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "sort") String sort,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime cursorCreatedAt,
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {

        if (userDetails == null) {
            throw new UnauthenticatedAccessException("로그인이 필요합니다.");
        }

        PagedResponse<WishListResponse> pagedResponse = queryFacade.getGroupBuyWishList(
                userDetails.getUser().getId(), sort, cursorCreatedAt, cursorId, limit);
        return ResponseEntity.ok(
                WrapperResponse.<PagedResponse<WishListResponse>>builder()
                        .message("관심 공구 리스트를 성공적으로 조회했습니다.")
                        .data(pagedResponse)
                        .build()
        );
    }

    /// 주최 공구 리스트 조회 SUCCESS
    @GetMapping("/users/me/hosts")
    public ResponseEntity<WrapperResponse<PagedResponse<HostedListResponse>>> getGroupBuyHostedList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {

        if (userDetails == null) {
            throw new UnauthenticatedAccessException("로그인이 필요합니다.");
        }

        PagedResponse<HostedListResponse> pagedResponse = queryFacade.getGroupBuyHostedList(
                userDetails.getUser().getId(), sort, cursorId, limit);
        return ResponseEntity.ok(
                WrapperResponse.<PagedResponse<HostedListResponse>>builder()
                        .message("주최 공구 리스트를 성공적으로 조회했습니다.")
                        .data(pagedResponse)
                        .build()
        );
    }

    /// 참여 공구 리스트 조회 SUCCESS V2 update - wish SUCCESS, 커서 적용 필요
    @GetMapping("/users/me/participants")
    public ResponseEntity<WrapperResponse<PagedResponse<ParticipatedListResponse>>> getGroupBuyParticipatedList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "sort") String sort,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime cursorCreatedAt,
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {

        if (userDetails == null) {
            throw new UnauthenticatedAccessException("로그인이 필요합니다.");
        }

        PagedResponse<ParticipatedListResponse> pagedResponse = queryFacade.getGroupBuyParticipatedList(
                userDetails.getUser().getId(), sort, cursorCreatedAt, cursorId, limit);
        return ResponseEntity.ok(
                WrapperResponse.<PagedResponse<ParticipatedListResponse>>builder()
                        .message("참여 공구 리스트를 성공적으로 조회했습니다.")
                        .data(pagedResponse)
                        .build()
        );
    }

    /// 공구 참여자 조회 SUCCESS
    @GetMapping("/{postId}/participants")
    public ResponseEntity<WrapperResponse<ParticipantListResponse>> getGroupBuyParticipantsInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {

        if (userDetails == null) {
            throw new UnauthenticatedAccessException("로그인이 필요합니다.");
        }

        ParticipantListResponse participantList = queryFacade.getGroupBuyParticipantsInfo(
                userDetails.getUser().getId(), postId);
        return ResponseEntity.ok(
                WrapperResponse.<ParticipantListResponse>builder()
                        .message("공구 참여자 리스트를 성공적으로 조회했습니다.")
                        .data(participantList)
                        .build()
        );
    }
}
