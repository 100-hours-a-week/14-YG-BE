package com.moogsan.moongsan_backend.domain.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.WishList.WishListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.query.GroupBuyQueryFacade;
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
@RequestMapping("/api/group-buys/users/me/wishes")
public class GroupBuyWishListController {

    private final GroupBuyQueryFacade queryFacade;

    @GetMapping
    public ResponseEntity<WrapperResponse<PagedResponse<WishListResponse>>> getGroupBuyWishList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "sort") String sort,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorCreatedAt,
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
}

