package com.moogsan.moongsan_backend.participantchat.application.facade.command;

import com.moogsan.moongsan_backend.participantchat.presentation.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.participantchat.application.service.command.CreateChatMessage;
import com.moogsan.moongsan_backend.participantchat.application.service.command.JoinChatRoom;
import com.moogsan.moongsan_backend.participantchat.application.service.command.LeaveChatRoom;
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
    public void joinChatRoom(User user, Long groupBuyId) {
         joinChatRoom.joinChatRoom(user, groupBuyId);
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
