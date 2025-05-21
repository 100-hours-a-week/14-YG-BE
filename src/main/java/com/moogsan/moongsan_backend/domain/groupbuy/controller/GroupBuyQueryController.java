package com.moogsan.moongsan_backend.domain.groupbuy.controller;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.UserAccountResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.UserProfileResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.BasicList.BasicListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipantList.ParticipantListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipatedList.ParticipatedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.WishList.WishListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyUpdate.GroupBuyForUpdateResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.*;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys")
public class GroupBuyQueryController {
    private final GetGroupBuyEditInfo getGroupBuyEditInfo;
    private final GetGroupBuyDetailInfo getGroupBuyDetailInfo;
    private final GetGroupBuyListByCursor getGroupBuyListByCursor;
    private final GetGroupBuyWishList getGroupBuyWishList;
    private final GetGroupBuyHostedList getGroupBuyHostedList;
    private final GetGroupBuyParticipatedList getGroupBuyParticipatedList;
    private final GetGroupBuyParticipantsInfo getGroupBuyParticipantsInfo;
    private final GetGroupBuyHostAccountInfo getGroupBuyHostAccountInfo;


    /// 공구 게시글 수정 전 정보 조회 SUCCESS
    @GetMapping("/{postId}/edit")
    public ResponseEntity<WrapperResponse<GroupBuyForUpdateResponse>> getGroupBuyEditInfo(@PathVariable Long postId) {
        GroupBuyForUpdateResponse groupBuyForUpdate = getGroupBuyEditInfo.getGroupBuyEditInfo(postId);
        return ResponseEntity.ok(
                WrapperResponse.<GroupBuyForUpdateResponse>builder()
                        .message("공구 게시글 수정을 성공적으로 조회했습니다.")
                        .data(groupBuyForUpdate)
                        .build()
        );
    }

    /// 공구 게시글 상세 조회 V2 update - wish SUCCESS
    @GetMapping("/{postId}")
    public ResponseEntity<WrapperResponse<DetailResponse>> getGroupBuyDetailInfo(
        @PathVariable Long postId,
        @AuthenticationPrincipal CustomUserDetails principal) {

        Long userId = (principal != null) ? principal.getUser().getId() : null;

        DetailResponse detail = getGroupBuyDetailInfo.getGroupBuyDetailInfo(userId, postId);
        return ResponseEntity.ok(
                WrapperResponse.<DetailResponse>builder()
                        .message("공구 게시글 상세 정보를 성공적으로 조회했습니다.")
                        .data(detail)
                        .build()
        );
    }

    @GetMapping("/{postId}/host/account")
    public ResponseEntity<WrapperResponse<UserAccountResponse>> getGroupBuyHostAccountInfo(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long userId = (principal != null) ? principal.getUser().getId() : null;

        UserAccountResponse accountResponse = getGroupBuyHostAccountInfo.getGroupBuyHostAccountInfo(userId, postId);
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
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {
        Long userId = (principal != null) ? principal.getUser().getId() : null;

        PagedResponse<BasicListResponse> pagedResponse =
                getGroupBuyListByCursor.getGroupBuyListByCursor(userId, categoryId, orderBy,
                        cursorId, cursorCreatedAt, cursorPrice, limit);
        return ResponseEntity.ok(
                WrapperResponse.<PagedResponse<BasicListResponse>>builder()
                        .message("전체 리스트를 성공적으로 조회했습니다.")
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
        PagedResponse<WishListResponse> pagedResponse = getGroupBuyWishList.getGroupBuyWishList(
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
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {
        PagedResponse<HostedListResponse> pagedResponse = getGroupBuyHostedList.getGroupBuyHostedList(
                userDetails.getUser().getId(), sort, cursor, limit);
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
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {
        PagedResponse<ParticipatedListResponse> pagedResponse = getGroupBuyParticipatedList.getGroupBuyParticipatedList(
                userDetails.getUser().getId(), sort, cursorCreatedAt, cursor, limit);
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
        ParticipantListResponse participantList = getGroupBuyParticipantsInfo.getGroupBuyParticipantsInfo(
                userDetails.getUser().getId(), postId);
        return ResponseEntity.ok(
                WrapperResponse.<ParticipantListResponse>builder()
                        .message("공구 참여자 리스트를 성공적으로 조회했습니다.")
                        .data(participantList)
                        .build()
        );
    }
}
