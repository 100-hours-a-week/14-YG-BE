package com.moogsan.moongsan_backend.domain.chatting.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.command.response.CommandChattingReponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetLatestMessages;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class GetLatestMessagesController {

    private final GetLatestMessages getLatestMessages;

    @GetMapping("/{chatRoomId}/polling/latest")
    public ResponseEntity<WrapperResponse<DeferredResult<List<ChatMessageResponse>>>> getLatestMessages(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) String lastMessageId
    ) {
        DeferredResult<List<ChatMessageResponse>> result = getLatestMessages.getLatesetMessages(currentUser.getUser(), chatRoomId, lastMessageId);
        return ResponseEntity.ok(
                WrapperResponse.<DeferredResult<List<ChatMessageResponse>>>builder()
                        .message("최신 메세지가 성공적으로 조회되었습니다.")
                        .data(result)
                        .build());
    }
}
