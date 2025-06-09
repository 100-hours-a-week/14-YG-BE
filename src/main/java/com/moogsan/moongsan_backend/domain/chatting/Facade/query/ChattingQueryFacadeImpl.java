package com.moogsan.moongsan_backend.domain.chatting.Facade.query;

import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatRoomResponse;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetChatRoomList;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetLatestMessageSse;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetLatestMessages;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetPastMessages;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChattingQueryFacadeImpl implements ChattingQueryFacade{

    private final GetPastMessages getPastMessages;
    private final GetLatestMessages getLatestMessages;
    private final GetLatestMessageSse getLatestMessagesSse;
    private final GetChatRoomList getChatRoomList;

    @Override
    public ChatMessagePageResponse getPastMessages(
            User user,
            Long chatRoomId,
            String cursorId
    ) {
        return getPastMessages.getPastMessages(user, chatRoomId, cursorId);
    }

    public DeferredResult<List<ChatMessageResponse>> getLatesetMessages(
            User currentUser, Long chatRoomId, String lastMessageId
    ) {
        return getLatestMessages.getLatesetMessages(currentUser, chatRoomId, lastMessageId);
    }

    public SseEmitter getLatestMessagesSse(
            User currentUser, Long chatRoomId, String lastMessageId
    ) {
        return getLatestMessagesSse.getLatestMessagesSse(currentUser, chatRoomId, lastMessageId);
    }

    public List<ChatRoomResponse> getChatRoomList (Long userId, LocalDateTime cursorJoinedAt, Long cursorId, Integer limit) {
        return getChatRoomList.getChatRoomList(userId, cursorJoinedAt, cursorId, limit);
    };
}
