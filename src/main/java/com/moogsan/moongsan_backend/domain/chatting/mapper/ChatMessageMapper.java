package com.moogsan.moongsan_backend.domain.chatting.mapper;

import com.moogsan.moongsan_backend.domain.chatting.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    // 메세지 작성
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

    // 메세지 조회
    public ChatMessageResponse toMessageResponse(ChatMessageDocument document, String nickname, String profileImageKey) {
        return ChatMessageResponse.builder()
                .messageId(document.getId())
                .participantId(document.getChatParticipantId())
                .nickname(nickname)
                .profileImageKey(profileImageKey)
                .messageContent(document.getContent())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
