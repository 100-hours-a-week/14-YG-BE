package com.moogsan.moongsan_backend.domain.chatting.participant.Facade.query;

import com.moogsan.moongsan_backend.domain.chatting.participant.dto.query.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.domain.chatting.participant.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.participant.dto.query.ChatRoomPagedResponse;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

public interface ChattingQueryFacade {
    ChatMessagePageResponse getPastMessages(
            User user,
            Long chatRoomId,
            String cursorId,
            boolean isPrev
    );

    DeferredResult<List<ChatMessageResponse>> getLatestMessages(
            User currentUser,
            Long chatRoomId,
            String lastMessageId
    );

    SseEmitter getLatestMessagesSse(
            User currentUser,
            Long chatRoomId
    );

    ChatRoomPagedResponse getChatRoomList (
            Long userId,
            LocalDateTime cursorJoinedAt,
            Integer limit)
    ;
}
