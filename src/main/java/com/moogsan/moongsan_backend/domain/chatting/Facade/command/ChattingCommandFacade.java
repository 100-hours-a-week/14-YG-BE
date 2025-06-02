package com.moogsan.moongsan_backend.domain.chatting.Facade.command;

import com.moogsan.moongsan_backend.domain.chatting.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.domain.user.entity.User;

public interface ChattingCommandFacade {
    Long joinChatRoom(User user, Long groupBuyId);

    void createChatMessage(User user, CreateChatMessageRequest request, Long chatRoomId);

    void leaveChatRoom(User user, Long groupBuyId);
}
