package com.moogsan.moongsan_backend.domain.chatting.Facade.command;

import com.moogsan.moongsan_backend.domain.chatting.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.domain.chatting.service.command.CreateChatMessage;
import com.moogsan.moongsan_backend.domain.chatting.service.command.JoinChatRoom;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChattingCommandFacadeImpl implements ChattingCommandFacade {

    private final CreateChatMessage createChatMessage;
    private final JoinChatRoom joinChatRoom;

    @Override
    public void joinChatRoom(User user, Long groupBuyId) {
        joinChatRoom.joinChatRoom(user, groupBuyId);
    }

    @Override
    public void createChatMessage(User user, CreateChatMessageRequest request, Long chatRoomId) {
        createChatMessage.createChatMessage(user, request, chatRoomId);
    }
}
