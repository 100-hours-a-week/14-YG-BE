package com.moogsan.moongsan_backend.domain.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.query.GroupBuyQueryFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.GET_DETAIL_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys/{postId}")
public class GroupBuyDetailController {

    private final GroupBuyQueryFacade queryFacade;

    @GetMapping
    public ResponseEntity<WrapperResponse<DetailResponse>> getGroupBuyDetailInfo(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = (userDetails != null) ? userDetails.getUser().getId() : null;
        DetailResponse detail = queryFacade.getGroupBuyDetailInfo(userId, postId);
        return ResponseEntity.ok(
                WrapperResponse.<DetailResponse>builder()
                        .message(GET_DETAIL_SUCCESS)
                        .data(detail)
                        .build()
        );
    }
}