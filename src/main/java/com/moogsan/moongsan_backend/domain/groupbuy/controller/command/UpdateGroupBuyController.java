package com.moogsan.moongsan_backend.domain.groupbuy.controller.command;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.response.CommandGroupBuyResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.command.GroupBuyCommandFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.GroupBuyResponseMessage.UPDATE_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys")
public class UpdateGroupBuyController {

    private final GroupBuyCommandFacade groupBuyFacade;

    @PatchMapping("/{postId}")
    public ResponseEntity<WrapperResponse<CommandGroupBuyResponse>> updateGroupBuy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateGroupBuyRequest request,
            @PathVariable Long postId) {

        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        groupBuyFacade.updateGroupBuy(userDetails.getUser(), request, postId);

        return ResponseEntity.ok(
                WrapperResponse.<CommandGroupBuyResponse>builder()
                        .message(UPDATE_SUCCESS)
                        .data(new CommandGroupBuyResponse(postId))
                        .build());
    }
}
