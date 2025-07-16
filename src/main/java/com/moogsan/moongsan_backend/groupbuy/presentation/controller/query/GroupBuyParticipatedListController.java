package com.moogsan.moongsan_backend.groupbuy.presentation.controller.query;

import com.moogsan.moongsan_backend.global.dto.WrapperResponse;
import com.moogsan.moongsan_backend.global.security.annotation.RequireLogin;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.ParticipatedList.ParticipatedListResponse;
import com.moogsan.moongsan_backend.groupbuy.application.facade.query.GroupBuyQueryFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.GET_PARTICIPATED_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys/users/me/participants")
@RequireLogin
public class GroupBuyParticipatedListController {

    private final GroupBuyQueryFacade queryFacade;

    @GetMapping
    public ResponseEntity<WrapperResponse<PagedResponse<ParticipatedListResponse>>> getGroupBuyParticipatedList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "sort") String sort,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorCreatedAt,
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {

        PagedResponse<ParticipatedListResponse> pagedResponse = queryFacade.getGroupBuyParticipatedList(
                userDetails.getUser().getId(), sort, cursorCreatedAt, cursorId, limit);
        return ResponseEntity.ok(
                WrapperResponse.<PagedResponse<ParticipatedListResponse>>builder()
                        .message(GET_PARTICIPATED_SUCCESS)
                        .data(pagedResponse)
                        .build()
        );
    }
}
