package com.moogsan.moongsan_backend.domain.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyUpdate.GroupBuyForUpdateResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.query.GroupBuyQueryFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.GET_UPDATE_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys/{postId}/edit")
public class GroupBuyEditController {

    private final GroupBuyQueryFacade queryFacade;

    @GetMapping
    public ResponseEntity<WrapperResponse<GroupBuyForUpdateResponse>> getGroupBuyEditInfo(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new UnauthenticatedAccessException("로그인이 필요합니다.");
        }
        GroupBuyForUpdateResponse groupBuyForUpdate = queryFacade.getGroupBuyEditInfo(userDetails.getUser().getId(), postId);
        return ResponseEntity.ok(
                WrapperResponse.<GroupBuyForUpdateResponse>builder()
                        .message(GET_UPDATE_SUCCESS)
                        .data(groupBuyForUpdate)
                        .build()
        );
    }
}
