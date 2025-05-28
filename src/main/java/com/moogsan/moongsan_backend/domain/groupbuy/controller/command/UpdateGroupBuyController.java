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
                        .message("공구 게시글이 성공적으로 수정되었습니다.")
                        .data(new CommandGroupBuyResponse(postId))
                        .build());
    }
}
