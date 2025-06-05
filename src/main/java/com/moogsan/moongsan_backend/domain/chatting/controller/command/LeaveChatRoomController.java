package com.moogsan.moongsan_backend.domain.chatting.controller.command;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.chatting.Facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.domain.chatting.dto.command.response.CommandChattingReponse;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats/participant")
public class LeaveChatRoomController {

    private final ChattingCommandFacade chattingCommandFacade;

    @DeleteMapping("/{postId}")
    public ResponseEntity<WrapperResponse<CommandChattingReponse>> leaveChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId
    ) {
        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        chattingCommandFacade.leaveChatRoom(userDetails.getUser(), postId);

        return ResponseEntity.ok(
                WrapperResponse.<CommandChattingReponse>builder()
                        .message("참여자 채팅방을 성공적으로 나갔습니다.")
                        .build());
    }
}
