package com.moogsan.moongsan_backend.domain.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.query.GroupBuyQueryFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys/users/me/hosts")
public class GroupBuyHostedListController {

    private final GroupBuyQueryFacade queryFacade;

    @GetMapping
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
}

