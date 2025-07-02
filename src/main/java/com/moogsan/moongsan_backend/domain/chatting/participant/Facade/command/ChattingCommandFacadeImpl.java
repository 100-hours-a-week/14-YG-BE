package com.moogsan.moongsan_backend.domain.chatting.participant.Facade.command;

import com.moogsan.moongsan_backend.domain.chatting.participant.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.domain.chatting.participant.service.command.CreateChatMessage;
import com.moogsan.moongsan_backend.domain.chatting.participant.service.command.JoinChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.participant.service.command.LeaveChatRoom;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChattingCommandFacadeImpl implements ChattingCommandFacade {

    private final CreateChatMessage createChatMessage;
    private final JoinChatRoom joinChatRoom;
    private final LeaveChatRoom leaveChatRoom;

    @Override
    public Long joinChatRoom(User user, Long groupBuyId) {
        return joinChatRoom.joinChatRoom(user, groupBuyId);
    }

    @Override
    public void createChatMessage(User user, CreateChatMessageRequest request, Long chatRoomId) {
        createChatMessage.createChatMessage(user, request, chatRoomId);
    }

    @Override
    public void leaveChatRoom(User user, Long groupBuyId) {
        leaveChatRoom.leaveChatRoom(user, groupBuyId);
    }


}
