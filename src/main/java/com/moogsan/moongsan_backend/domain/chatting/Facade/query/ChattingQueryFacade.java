package com.moogsan.moongsan_backend.domain.chatting.Facade.query;

import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.user.entity.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface ChattingQueryFacade {
    ChatMessagePageResponse pollMessages(
            User user,
            Long chatRoomId,
            String cursorId
    );
}
