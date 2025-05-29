package com.moogsan.moongsan_backend.domain.chatting.controller.command;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.chatting.Facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.domain.chatting.dto.command.response.CommandChattingReponse;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class JoinChatRoomController {

    private final ChattingCommandFacade chattingCommandFacade;

    @PostMapping("/{postId}")
    public ResponseEntity<WrapperResponse<CommandChattingReponse>> joinChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId
    ) {
        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        chattingCommandFacade.joinChatRoom(userDetails.getUser(), postId);

        return ResponseEntity.ok(
                WrapperResponse.<CommandChattingReponse>builder()
                        .message("참여자 채팅방에 성공적으로 참여하였습니다.")
                        .build());
    }
}
