package com.moogsan.moongsan_backend.groupbuy.presentation.controller.query;

import com.moogsan.moongsan_backend.global.dto.WrapperResponse;
import com.moogsan.moongsan_backend.global.security.annotation.RequireLogin;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.groupbuy.application.facade.query.GroupBuyQueryFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.GET_HOSTED_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys/users/me/hosts")
@RequireLogin
public class GroupBuyHostedListController {

    private final GroupBuyQueryFacade queryFacade;

    @GetMapping
    public ResponseEntity<WrapperResponse<PagedResponse<HostedListResponse>>> getGroupBuyHostedList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {

        PagedResponse<HostedListResponse> pagedResponse = queryFacade.getGroupBuyHostedList(
                userDetails.getUser().getId(), sort, cursorId, limit);
        return ResponseEntity.ok(
                WrapperResponse.<PagedResponse<HostedListResponse>>builder()
                        .message(GET_HOSTED_SUCCESS)
                        .data(pagedResponse)
                        .build()
        );
    }
}

