package com.moogsan.moongsan_backend.domain.chatting.mapper;

import com.moogsan.moongsan_backend.domain.chatting.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageCommandMapper {

    // 참여자 채팅방 메세지 작성
    public ChatMessageDocument toMessageDocument(ChatRoom chatRoom, Long participantId, CreateChatMessageRequest request, Long nextSeq) {
        return ChatMessageDocument.builder()
                .id(null)
                .chatRoomId(chatRoom.getId())
                .chatParticipantId(participantId)
                .content(request.getMessageContent())
                .messageSeq(nextSeq)
                .viewCount(chatRoom.getParticipantsCount())
                .build();
    }
}
