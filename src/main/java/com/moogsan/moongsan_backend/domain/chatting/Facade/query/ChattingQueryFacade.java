package com.moogsan.moongsan_backend.domain.chatting.Facade.query;

import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatRoomResponse;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

public interface ChattingQueryFacade {
    ChatMessagePageResponse getPastMessages(
            User user,
            Long chatRoomId,
            String cursorId
    );

    DeferredResult<List<ChatMessageResponse>> getLatesetMessages(
            User currentUser,
            Long chatRoomId,
            String lastMessageId
    );

    SseEmitter getLatestMessagesSse(
            User currentUser,
            Long chatRoomId,
            String lastMessageId
    );

    List<ChatRoomResponse> getChatRoomList (
            Long userId,
            LocalDateTime cursorJoinedAt,
            Long cursorId,
            Integer limit);
}
