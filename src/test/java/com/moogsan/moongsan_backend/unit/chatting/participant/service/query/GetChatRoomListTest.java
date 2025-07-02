package com.moogsan.moongsan_backend.unit.chatting.participant.service.query;

import com.moogsan.moongsan_backend.domain.chatting.participant.dto.query.ChatRoomPagedResponse;
import com.moogsan.moongsan_backend.domain.chatting.participant.dto.query.ChatRoomResponse;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.participant.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.participant.service.query.GetChatRoomList;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetChatRoomListTest {

    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    @Mock
    private ChatMessageQueryMapper chatMessageQueryMapper;

    private GetChatRoomList getChatRoomList;
    private User loginedUser;
    private Pageable expectedPageable;
    private LocalDateTime cursorJoinedAt;
    private int limit;
    private ChatRoom chatRoom;
    private ChatParticipant participant1;
    private ChatParticipant participant2;
    private ChatParticipant participant3;

    @BeforeEach
    void setUp() {
        loginedUser = User.builder().id(1L).build();
        chatRoom = ChatRoom.builder().id(29L).build();

        participant1 = ChatParticipant.builder()
                .id(1L)
                .chatRoom(chatRoom)
                .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .build();
        participant2 = ChatParticipant.builder()
                .id(2L)
                .chatRoom(chatRoom)
                .joinedAt(LocalDateTime.of(2025, 1, 1, 9, 0))
                .build();
        participant3 = ChatParticipant.builder()
                .id(3L)
                .chatRoom(chatRoom)
                .joinedAt(LocalDateTime.of(2025, 1, 1, 8, 0))
                .build();

        getChatRoomList = new GetChatRoomList(
                chatParticipantRepository,
                chatMessageQueryMapper
        );
    }

    @Test
    @DisplayName("참여자 채팅방 리스트 조회 성공 - 로그인한 유저, 커서 존재, 다음 요소 있음")
    void getChatRoomList_success_user_cursor_has_next() {
        // Given
        cursorJoinedAt = LocalDateTime.of(2025, 1, 1, 9, 30);
        limit = 2;
        List<ChatParticipant> participants = List.of(participant1, participant2, participant3);
        expectedPageable = PageRequest.of(0, limit + 1,
                Sort.by("joinedAt").descending()
                        .and(Sort.by("id").descending())
        );
        List<ChatParticipant> pageOf = participants.subList(0, limit);
        List<ChatRoom> rooms = pageOf.stream()
                .map(ChatParticipant::getChatRoom)
                .toList();
        ChatRoomResponse response1 = ChatRoomResponse.builder().build();
        ChatRoomResponse response2 = ChatRoomResponse.builder().build();
        List<ChatRoomResponse> mappedResponses = List.of(response1, response2);
        LocalDateTime lastJoinedAt = pageOf.getLast().getJoinedAt();
        boolean hasMore = participants.size() > limit;

        when(chatParticipantRepository.findParticipantsAfter(loginedUser.getId(), cursorJoinedAt, expectedPageable))
                .thenReturn(participants);
        when(chatMessageQueryMapper.toChatRoomList(rooms))
                .thenReturn(mappedResponses);

        ChatRoomPagedResponse result = getChatRoomList.getChatRoomList(
                loginedUser.getId(),
                cursorJoinedAt,
                limit
        );

        verify(chatParticipantRepository, times(1))
                .findParticipantsAfter(loginedUser.getId(), cursorJoinedAt, expectedPageable);
        verify(chatMessageQueryMapper, times(1)).toChatRoomList(rooms);
        assertThat(result.getChatRooms()).hasSize(limit);
        assertThat(result.getNextCursorJoinedAt()).isEqualTo(lastJoinedAt);
        assertThat(result.isHasMore()).isEqualTo(hasMore);
    }

    @Test
    @DisplayName("참여자 채팅방 리스트 조회 성공 - 로그인한 유저, 커서 없음, 다음 요소 있음")
    void getChatRoomList_success_user_noCursor_has_next() {

        cursorJoinedAt = null;
        limit = 2;
        List<ChatParticipant> participants = List.of(participant1, participant2, participant3);
        expectedPageable = PageRequest.of(0, limit + 1,
                Sort.by("joinedAt").descending()
                        .and(Sort.by("id").descending())
        );
        List<ChatParticipant> pageOf = participants.subList(0, limit);
        List<ChatRoom> rooms = pageOf.stream()
                .map(ChatParticipant::getChatRoom)
                .toList();
        ChatRoomResponse response1 = ChatRoomResponse.builder().build();
        ChatRoomResponse response2 = ChatRoomResponse.builder().build();
        List<ChatRoomResponse> mappedResponses = List.of(response1, response2);
        LocalDateTime lastJoinedAt = pageOf.getLast().getJoinedAt();
        boolean hasMore = participants.size() > limit;

        when(chatParticipantRepository.findInitialParticipants(loginedUser.getId(), expectedPageable))
                .thenReturn(participants);
        when(chatMessageQueryMapper.toChatRoomList(rooms))
                .thenReturn(mappedResponses);

        ChatRoomPagedResponse result = getChatRoomList.getChatRoomList(
                loginedUser.getId(),
                null,
                limit
        );

        verify(chatParticipantRepository, times(1))
                .findInitialParticipants(loginedUser.getId(), expectedPageable);
        verify(chatMessageQueryMapper, times(1)).toChatRoomList(rooms);
        assertThat(result.getChatRooms()).hasSize(limit);
        assertThat(result.getNextCursorJoinedAt()).isEqualTo(lastJoinedAt);
        assertThat(result.isHasMore()).isEqualTo(hasMore);
    }

    @Test
    @DisplayName("참여자 채팅방 리스트 조회 성공 - 로그인한 유저, 커서 있음, 다음 요소 없음")
    void getChatRoomList_success_user_cursor_no_next() {
        cursorJoinedAt = LocalDateTime.of(2025, 1, 1, 9, 30);
        limit = 2;
        List<ChatParticipant> participants = List.of(participant1, participant2); // size == limit
        expectedPageable = PageRequest.of(0, limit + 1,
                Sort.by("joinedAt").descending()
                        .and(Sort.by("id").descending())
        );
        List<ChatRoom> rooms = participants.stream()
                .map(ChatParticipant::getChatRoom)
                .toList();
        ChatRoomResponse response1 = ChatRoomResponse.builder().build();
        ChatRoomResponse response2 = ChatRoomResponse.builder().build();
        List<ChatRoomResponse> mappedResponses = List.of(response1, response2);
        LocalDateTime lastJoinedAt = participants.getLast().getJoinedAt();
        boolean hasMore = false; // participants.size() == limit

        when(chatParticipantRepository.findParticipantsAfter(loginedUser.getId(), cursorJoinedAt, expectedPageable))
                .thenReturn(participants);
        when(chatMessageQueryMapper.toChatRoomList(rooms))
                .thenReturn(mappedResponses);

        ChatRoomPagedResponse result = getChatRoomList.getChatRoomList(
                loginedUser.getId(),
                cursorJoinedAt,
                limit
        );

        verify(chatParticipantRepository, times(1))
                .findParticipantsAfter(loginedUser.getId(), cursorJoinedAt, expectedPageable);
        verify(chatMessageQueryMapper, times(1)).toChatRoomList(rooms);
        assertThat(result.getChatRooms()).hasSize(limit);
        assertThat(result.getNextCursorJoinedAt()).isEqualTo(lastJoinedAt);
        assertThat(result.isHasMore()).isFalse();
    }

    @Test
    @DisplayName("참여자 채팅방 리스트 조회 성공 - 로그인한 유저, 결과 없음")
    void getChatRoomList_success_user_empty() {

        cursorJoinedAt = null;
        limit = 5;
        List<ChatParticipant> participants = Collections.emptyList();
        expectedPageable = PageRequest.of(0, limit + 1,
                Sort.by("joinedAt").descending()
                        .and(Sort.by("id").descending())
        );
        List<ChatRoom> rooms = Collections.emptyList();
        List<ChatRoomResponse> mappedResponses = Collections.emptyList();

        when(chatParticipantRepository.findInitialParticipants(loginedUser.getId(), expectedPageable))
                .thenReturn(participants);
        when(chatMessageQueryMapper.toChatRoomList(rooms))
                .thenReturn(mappedResponses);

        ChatRoomPagedResponse result = getChatRoomList.getChatRoomList(
                loginedUser.getId(),
                null,
                limit
        );

        verify(chatParticipantRepository, times(1))
                .findInitialParticipants(loginedUser.getId(), expectedPageable);
        verify(chatMessageQueryMapper, times(1)).toChatRoomList(rooms);
        assertThat(result.getChatRooms()).isEmpty();
        assertThat(result.getNextCursorJoinedAt()).isNull();
        assertThat(result.isHasMore()).isFalse();
    }
}
