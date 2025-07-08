package com.moogsan.moongsan_backend.domain.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.UserAccountResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.query.GroupBuyQueryFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.GET_ACCOUNT_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys/{postId}/host/account")
public class GroupBuyHostAccountController {

    private final GroupBuyQueryFacade queryFacade;

    @GetMapping
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
                        .message(GET_ACCOUNT_SUCCESS)
                        .data(accountResponse)
                        .build()
        );
    }
}
