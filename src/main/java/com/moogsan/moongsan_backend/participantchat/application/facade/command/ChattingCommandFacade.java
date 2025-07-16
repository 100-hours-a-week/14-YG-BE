package com.moogsan.moongsan_backend.participantchat.application.facade.command;

import com.moogsan.moongsan_backend.participantchat.presentation.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.domain.user.entity.User;

public interface ChattingCommandFacade {
    void joinChatRoom(User user, Long groupBuyId);

    void createChatMessage(User user, CreateChatMessageRequest request, Long chatRoomId);

    void leaveChatRoom(User user, Long groupBuyId);
}
