package com.moogsan.moongsan_backend.domain.chatting.Facade.query;

import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.service.query.PollMessages;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChattingQueryFacadeImpl implements ChattingQueryFacade{

    private final PollMessages pollMessages;

    @Override
    public ChatMessagePageResponse pollMessages(
            User user,
            Long chatRoomId,
            String cursorId
    ) {
        pollMessages.pollMessages(user, chatRoomId, cursorId);
        return pollMessages.pollMessages(user, chatRoomId, cursorId);
    }
}
