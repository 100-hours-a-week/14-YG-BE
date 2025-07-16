package com.moogsan.moongsan_backend.groupbuy.presentation.controller.command;

import com.moogsan.moongsan_backend.global.dto.EmptyResponse;
import com.moogsan.moongsan_backend.global.dto.WrapperResponse;
import com.moogsan.moongsan_backend.global.security.annotation.RequireLogin;
import com.moogsan.moongsan_backend.groupbuy.application.facade.command.GroupBuyCommandFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.LEAVE_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys")
@RequireLogin
public class LeaveGroupBuyController {

    private final GroupBuyCommandFacade groupBuyFacade;

    @DeleteMapping("/{postId}/participants")
    public ResponseEntity<WrapperResponse<EmptyResponse>> leaveGroupBuy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {

        groupBuyFacade.leaveGroupBuy(userDetails.getUser(), postId);
        return ResponseEntity.ok(
                WrapperResponse.<EmptyResponse>builder()
                        .message(LEAVE_SUCCESS)
                        .build());
    }
}
