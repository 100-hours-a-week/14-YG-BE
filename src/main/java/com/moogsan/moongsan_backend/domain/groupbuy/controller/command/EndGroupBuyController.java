package com.moogsan.moongsan_backend.domain.groupbuy.controller.command;

import com.moogsan.moongsan_backend.domain.EmptyResponse;
import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.command.GroupBuyCommandFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.GroupBuyResponseMessage.END_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys")
public class EndGroupBuyController {

    private final GroupBuyCommandFacade groupBuyFacade;

    @PatchMapping("/{postId}/end")
    public ResponseEntity<WrapperResponse<EmptyResponse>> endGroupBuy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {

        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        groupBuyFacade.endGroupBuy(userDetails.getUser(), postId);

        return ResponseEntity.ok(
                WrapperResponse.<EmptyResponse>builder()
                        .message(END_SUCCESS)
                        .build());
    }
}
