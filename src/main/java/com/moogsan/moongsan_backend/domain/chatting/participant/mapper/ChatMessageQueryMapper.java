package com.moogsan.moongsan_backend.domain.chatting.participant.mapper;

import com.moogsan.moongsan_backend.domain.chatting.participant.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.participant.dto.query.ChatRoomResponse;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatMessageRepository;
import com.moogsan.moongsan_backend.domain.image.entity.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatMessageQueryMapper {

    private final ChatMessageRepository chatMessageRepository;

    // 참여자 채팅방 메세지 조회
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

    // 참여자 채팅방 리스트 조회
    public List<ChatRoomResponse> toChatRoomList(List<ChatRoom> rooms) {
        return rooms.stream()
                .map(chatRoom -> {
                    var groupBuy = chatRoom.getGroupBuy();
                    ChatMessageDocument lastMessage = chatMessageRepository
                            .findTopByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId());

                    String img = groupBuy.getImages().stream()
                            .findFirst()
                            .map(Image::getImageKey)
                            .orElse(null);

                    return ChatRoomResponse.builder()
                            .chatRoomId(chatRoom.getId())
                            .title(groupBuy.getTitle())
                            .location(groupBuy.getLocation())
                            .imagekey(img)
                            .lastMessageId(lastMessage != null ? lastMessage.getId() : null)
                            .lastMessageContent(lastMessage != null ? lastMessage.getContent() : null)
                            .soldAmount(groupBuy.getTotalAmount() - groupBuy.getLeftAmount())
                            .totalAmount(groupBuy.getTotalAmount())
                            .participantCount(chatRoom.getParticipantsCount())
                            .build();
                })
                .toList();
    }
}
