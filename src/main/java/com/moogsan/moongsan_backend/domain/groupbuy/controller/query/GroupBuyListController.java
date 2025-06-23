package com.moogsan.moongsan_backend.domain.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.BasicList.BasicListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.query.GroupBuyQueryFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.GET_LIST_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys")
public class GroupBuyListController {

    private final GroupBuyQueryFacade queryFacade;

    @GetMapping
    public ResponseEntity<WrapperResponse<PagedResponse<BasicListResponse>>> getGroupBuyListByCursor(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "orderBy", defaultValue = "created") String orderBy,
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "cursorCreatedAt", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorCreatedAt,
            @RequestParam(value = "cursorSoldRatio", required = false) Integer cursorSoldRatio,
            @RequestParam(value = "cursorPrice", required = false) Integer cursorPrice,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "openOnly", required = false) Boolean openOnly,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Long userId = (principal != null) ? principal.getUser().getId() : null;
        PagedResponse<BasicListResponse> pagedResponse =
                queryFacade.getGroupBuyListByCursor(userId, categoryId, orderBy,
                        cursorId, cursorCreatedAt, cursorSoldRatio, cursorPrice, limit, openOnly,keyword);
        return ResponseEntity.ok(
                WrapperResponse.<PagedResponse<BasicListResponse>>builder()
                        .message(GET_LIST_SUCCESS)
                        .data(pagedResponse)
                        .build()
        );
    }
}
