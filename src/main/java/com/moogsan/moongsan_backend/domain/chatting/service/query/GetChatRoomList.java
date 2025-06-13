package com.moogsan.moongsan_backend.domain.chatting.service.query;

import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatRoomPagedResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatRoomResponse;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetChatRoomList {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageQueryMapper chatMessageQueryMapper;

    public ChatRoomPagedResponse getChatRoomList (Long userId, LocalDateTime cursorJoinedAt, Integer limit) {

        // 결과 조회 -> 없으면 빈 리스트 리턴
        Pageable page = PageRequest.of(0,
                limit + 1,
                Sort.by("joinedAt").descending()
                        .and(Sort.by("id").descending())
        );

        List<ChatParticipant> participants;
        if (cursorJoinedAt == null) {
            participants = chatParticipantRepository.findInitialParticipants(userId, page);
        } else {
            participants = chatParticipantRepository.findParticipantsAfter(userId, cursorJoinedAt, page);
        }

        List<ChatParticipant> pageOf = participants.size() > limit
                ? participants.subList(0, limit)
                : participants;

        LocalDateTime nextJoinedAt = null;
        if (!pageOf.isEmpty()) {
            ChatParticipant last = pageOf.getLast();
            nextJoinedAt = last.getJoinedAt();
        }

        boolean hasMore = participants.size() > limit;

        List<ChatRoom> rooms = pageOf.stream()
                .map(ChatParticipant::getChatRoom)
                .toList();

        List<ChatRoomResponse> results = chatMessageQueryMapper.toChatRoomList(rooms);

        return ChatRoomPagedResponse.builder()
                .chatRooms(results)
                .nextCursorJoinedAt(nextJoinedAt)
                .hasMore(hasMore)
                .build();
    }
}
