package com.moogsan.moongsan_backend.domain.chatting.service.query;

import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatRoomResponse;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatMessageRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.image.entity.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetChatRoomList {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageQueryMapper chatMessageQueryMapper;

    public List<ChatRoomResponse> getChatRoomList (Long userId, LocalDateTime cursorJoinedAt, Long cursorId, Integer limit) {

        // 결과 조회 -> 없으면 빈 리스트 리턴
        Pageable pageable = PageRequest.of(0, limit);

        List<ChatRoom> rooms;
        if (cursorJoinedAt == null || cursorId == null) {
            rooms = chatParticipantRepository.findInitialChatRooms(userId, pageable);
        } else {
            rooms = chatParticipantRepository.findActiveParticipantChatRoomsByUserIdWithCursor(
                    userId,
                    cursorJoinedAt,
                    cursorId == null ? Long.MAX_VALUE : cursorId,
                    pageable
            );
        }

        return chatMessageQueryMapper.toChatRoomList(rooms);
    }
}
