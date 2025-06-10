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

import static com.moogsan.moongsan_backend.domain.groupbuy.message.GroupBuyResponseMessage.CREATE_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys")
public class CreateGroupBuyController {

    private final GroupBuyCommandFacade groupBuyFacade;

    @PostMapping
    public ResponseEntity<WrapperResponse<CommandGroupBuyResponse>> createGroupBuy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateGroupBuyRequest request) {

        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        Long postId = groupBuyFacade.createGroupBuy(userDetails.getUser(), request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(postId).toUri();

        return ResponseEntity.created(location)
                .body(WrapperResponse.<CommandGroupBuyResponse>builder()
                        .message(CREATE_SUCCESS)
                        .data(new CommandGroupBuyResponse(postId))
                        .build());
    }
}

