package com.moogsan.moongsan_backend.domain.chatting.controller.query;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.chatting.Facade.query.ChattingQueryFacade;
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

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats/participant")
public class GetLatestMessagesController {

    private final ChattingQueryFacade chattingQueryFacade;

    @GetMapping("/{chatRoomId}/polling/latest")
    public DeferredResult<WrapperResponse<List<ChatMessageResponse>>> getLatestMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) String lastMessageId
    ) {
        DeferredResult<WrapperResponse<List<ChatMessageResponse>>> wrapperResult = new DeferredResult<>(30_000L);

        DeferredResult<List<ChatMessageResponse>> rawResult = chattingQueryFacade.
                getLatesetMessages(userDetails.getUser(), chatRoomId, lastMessageId);

        rawResult.onTimeout(() -> {
            wrapperResult.setResult(
                    WrapperResponse.<List<ChatMessageResponse>>builder()
                            .message("타임아웃")
                            .data(Collections.emptyList())
                            .build()
            );
        });

        rawResult.setResultHandler(result -> {
            @SuppressWarnings("unchecked")
            List<ChatMessageResponse> messages = (List<ChatMessageResponse>) result;
            wrapperResult.setResult(
                    WrapperResponse.<List<ChatMessageResponse>>builder()
                            .message("최신 메세지를 성공적으로 수신하였습니다. <<long polling>>")
                            .data(messages)
                            .build()
            );
        });
        return wrapperResult;
    }
}