package com.moogsan.moongsan_backend.domain.groupbuy.controller.command;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.response.CommandGroupBuyResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.command.GroupBuyCommandFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.DuplicateRequestException;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import com.moogsan.moongsan_backend.global.lock.DuplicateRequestPreventer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys")
public class CreateGroupBuyController {

    private final GroupBuyCommandFacade groupBuyFacade;
    private final DuplicateRequestPreventer duplicateRequestPreventer;

    @PostMapping
    public ResponseEntity<WrapperResponse<CommandGroupBuyResponse>> createGroupBuy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateGroupBuyRequest request) {

        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        Long userId = userDetails.getUser().getId();
        String key = "group-buy:creating:" + userId;

        if (!duplicateRequestPreventer.tryAcquireLock(key, 3)) {
            throw new DuplicateRequestException();
        }

        Long postId = groupBuyFacade.createGroupBuy(userDetails.getUser(), request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(postId).toUri();

        return ResponseEntity.created(location)
                .body(WrapperResponse.<CommandGroupBuyResponse>builder()
                        .message("공구 게시글이 성공적으로 업로드되었습니다.")
                        .data(new CommandGroupBuyResponse(postId))
                        .build());
    }
}

