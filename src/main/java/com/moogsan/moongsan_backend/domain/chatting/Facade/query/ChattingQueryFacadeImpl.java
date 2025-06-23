package com.moogsan.moongsan_backend.domain.chatting.Facade.query;

import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatRoomPagedResponse;
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
            String cursorId,
            boolean isPrev
    ) {
        return getPastMessages.getPastMessages(user, chatRoomId, cursorId, isPrev);
    }

    @Override
    public DeferredResult<List<ChatMessageResponse>> getLatestMessages(
            User user, Long chatRoomId, String lastMessageId) {

        // 1) 검증 + 초기 조회 (트랜잭션)
        List<ChatMessageResponse> initial =
                getLatestMessages.getLatestMessages(user, chatRoomId, lastMessageId);
        if (!initial.isEmpty()) {
            DeferredResult<List<ChatMessageResponse>> dr = new DeferredResult<>();
            dr.setResult(initial);
            return dr;
        }

        // 2) lastMessageId가 들어오면 즉시 조회
        if (lastMessageId != null && !lastMessageId.isBlank()) {
            DeferredResult<List<ChatMessageResponse>> dr = new DeferredResult<>(0L);
            List<ChatMessageResponse> list = getLatestMessages.getLatestMessages(user, chatRoomId, lastMessageId);
            dr.setResult(list);
            return dr;
        }

        // 3) 롱폴링 대기 (노 트랜잭션)
        return getLatestMessages.createLongPollingResult(user, chatRoomId);
    }

    @Override
    public SseEmitter getLatestMessagesSse(
            User currentUser, Long chatRoomId
    ) {
        // 1) 검증만 수행 (트랜잭션 경계 안)
        return getLatestMessagesSse.getLatestMessagesSse(currentUser, chatRoomId);

        // 2) Emitter 생성 및 등록 (트랜잭션 경계 밖)
        // return getLatestMessagesSse.createEmitter(chatRoomId);
    }

    public ChatRoomPagedResponse getChatRoomList (Long userId, LocalDateTime cursorJoinedAt, Integer limit) {
        return getChatRoomList.getChatRoomList(userId, cursorJoinedAt, limit);
    };
}
