package com.moogsan.moongsan_backend.domain.chatting.participant.controller.command;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.chatting.participant.Facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.domain.chatting.participant.dto.command.response.CommandChattingReponse;
import com.moogsan.moongsan_backend.domain.chatting.participant.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats/participant")
public class CreateChatMessageController {
    private final ChattingCommandFacade chattingCommandFacade;

    @PostMapping("/{chatRoomId}/messages")
    public ResponseEntity<WrapperResponse<CommandChattingReponse>> createChatMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateChatMessageRequest request,
            @PathVariable Long chatRoomId
    ) {
        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        chattingCommandFacade.createChatMessage(userDetails.getUser(), request, chatRoomId);

        return ResponseEntity.ok(
                WrapperResponse.<CommandChattingReponse>builder()
                        .message("메세지가 성공적으로 작성되었습니다.")
                        .build());
    }
}