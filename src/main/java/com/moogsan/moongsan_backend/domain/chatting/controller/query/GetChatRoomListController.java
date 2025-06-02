package com.moogsan.moongsan_backend.domain.chatting.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.chatting.Facade.query.ChattingQueryFacade;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatRoomResponse;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class GetChatRoomListController {

    private final ChattingQueryFacade chattingQueryFacade;

    @GetMapping("/users/me/participant")
    public ResponseEntity<WrapperResponse<List<ChatRoomResponse>>> getChatRoomList (
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "cursorJoinedAt", required = false) LocalDateTime cursorJoinedAt,
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit
    ) {
        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        List<ChatRoomResponse> chatRoomResponse = chattingQueryFacade
                .getChatRoomList(userDetails.getUser().getId(), cursorJoinedAt, cursorId, limit);
        return ResponseEntity.ok(
                WrapperResponse.<List<ChatRoomResponse>>builder()
                        .message("참여자 채팅방을 성공적으로 조회했습니다")
                        .data(chatRoomResponse)
                        .build());

    }
}
