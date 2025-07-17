package com.moogsan.moongsan_backend.participantchat.application.service.query;

import com.moogsan.moongsan_backend.participantchat.presentation.dto.query.response.ChatRoomPagedResponse;
import com.moogsan.moongsan_backend.participantchat.presentation.dto.query.response.ChatRoomResponse;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatParticipant;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatRoom;
import com.moogsan.moongsan_backend.participantchat.application.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatParticipantRepository;
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

    public ChatRoomPagedResponse getChatRoomList (
            Long userId, LocalDateTime cursorJoinedAt, Integer limit) {

        // 참여자 조회
        List<ChatParticipant> participants = fetchAndValidate(userId, cursorJoinedAt, limit);

        // 더보기 여부 확인
        boolean hasMore = participants.size() > limit;

        // 실제 데이터 크기로 조정
        List<ChatParticipant> paginatedParticipants = participants.size() > limit ? participants.subList(0, limit) : participants;

        // 다음 커서 지정
        LocalDateTime nextJoinedAt = null;
        if (!paginatedParticipants.isEmpty()) {
            ChatParticipant last = paginatedParticipants.getLast();
            nextJoinedAt = last.getJoinedAt();
        }

        // 채팅방 매핑
         List<ChatRoom> rooms = extractChatRoom(paginatedParticipants);

        // DTO 매핑
        List<ChatRoomResponse> results = chatMessageQueryMapper.toChatRoomList(rooms);

        return ChatRoomPagedResponse.builder()
                .chatRooms(results)
                .nextCursorJoinedAt(nextJoinedAt)
                .hasMore(hasMore)
                .build();
    }

    private List<ChatParticipant> fetchAndValidate(Long userId, LocalDateTime cursorJoinedAt, Integer limit) {
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

        return participants;
    }

    private List<ChatRoom> extractChatRoom(List<ChatParticipant> paginatedParticipants) {
        return paginatedParticipants.stream()
                .map(ChatParticipant::getChatRoom)
                .toList();
    }
}
