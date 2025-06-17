package com.moogsan.moongsan_backend.unit.chatting.service.query;

import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.domain.chatting.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatMessageRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetLatestMessages;
import com.moogsan.moongsan_backend.domain.order.exception.specific.OrderNotFoundException;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.Duration;
import java.util.*;

import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetLatestMessagesTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageQueryMapper chatMessageQueryMapper;

    // FOR getLatestMessages
    private GetLatestMessages service;
    private User participantUser;
    private User normalUser;
    private User authorUser;
    private ChatRoom chatRoom;
    private String lastMessageId;
    private ChatMessageDocument message1;
    private ChatMessageDocument message2;
    private List<ChatMessageDocument> newMessages;

    // FOR createLongPollingResult - 단순 매핑이기 때문에 테스트하지 않기로 결정 -

    // FOR notifyNewMessage
    private SecurityContext context;
    private DeferredResult<List<ChatMessageResponse>> result1;
    private DeferredResult<List<ChatMessageResponse>> result2;
    private ChatMessageResponse response;


    @BeforeEach
    void setUp() {

        participantUser = User.builder().id(1L).build();
        normalUser = User.builder().id(2L).build();
        authorUser =  User.builder().id(3L).nickname("lucy").imageKey("images/image1").build();
        chatRoom = ChatRoom.builder().id(20L).type("PARTICIPANT").build();
        lastMessageId = "64a6f2c21f9b8e4d3a9b1233";
        message1 = ChatMessageDocument.builder().id("64a6f2c21f9b8e4d3a9b1234").chatRoomId(chatRoom.getId()).build();
        message2 = ChatMessageDocument.builder().id("64a6f2c21f9b8e4d3a9b1235").chatRoomId(chatRoom.getId()).build();
        newMessages = List.of(message1, message2);
        service = new GetLatestMessages(
                chatMessageRepository,
                chatParticipantRepository,
                chatRoomRepository,
                chatMessageQueryMapper
        );

        result1 = service.createLongPollingResult(chatRoom.getId());
        result2 = service.createLongPollingResult(chatRoom.getId());
        response = ChatMessageResponse.builder().build();
        context = mock(SecurityContext.class);
    }

    @Nested
    @DisplayName("Describe GetLatestMessages")
    class DescribeGetLatestMessages {

        @Test
        @DisplayName("최신 메세지 조회 성공 (롱폴링) - 참여자")
        void get_lastest_message_success_participant () {
            when(chatRoomRepository.findById(20L)).thenReturn(Optional.of(chatRoom));
            when(chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId()))
                    .thenReturn(true);
            when(chatMessageRepository.findMessagesAfter(chatRoom.getId(), lastMessageId))
                    .thenReturn(newMessages);

            service.getLatestMessages(participantUser, 20L, lastMessageId);

            verify(chatRoomRepository, times(1)).findById(20L);
            verify(chatParticipantRepository, times(1)).existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());
            verify(chatMessageRepository, times(1)).findMessagesAfter(chatRoom.getId(), lastMessageId);

        }

        @Test
        @DisplayName("최신 메세지 조회 실패 (롱폴링) - 존재하지 않는 채팅방")
        void get_lastest_message_fail_chat_room_not_exist () {
            when(chatRoomRepository.findById(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getLatestMessages(normalUser, 20L, lastMessageId))
                    .isInstanceOf(ChatRoomNotFoundException.class)
                    .hasMessageContaining(CHAT_ROOM_NOT_FOUND);

            verify(chatRoomRepository, times(1)).findById(20L);
        }

        @Test
        @DisplayName("최신 메세지 조회 실패 (롱폴링) - 참여자가 아님")
        void get_lastest_message_fail_normal_user() {
            when(chatRoomRepository.findById(20L)).thenReturn(Optional.of(chatRoom));
            when(chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), normalUser.getId()))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.getLatestMessages(normalUser, 20L, lastMessageId))
                    .isInstanceOf(NotParticipantException.class)
                    .hasMessageContaining(NOT_PARTICIPANT);

            verify(chatRoomRepository, times(1)).findById(20L);
            verify(chatParticipantRepository, times(1)).existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), normalUser.getId());
        }
    }

    @Nested
    @DisplayName("Describe NotifyNewMessage")
    class DescribeNotifyNewMessage {  ///  비동기 awaitility

        @Test
        @DisplayName("새로운 메세지 발행 성공 - 단일 리스너, 큐 비우기 확인")
        void notify_new_messages_success_single_listener () {
            when(chatMessageQueryMapper.toMessageResponse(message1, authorUser.getNickname(), authorUser.getImageKey()))
                    .thenReturn(response);

            service.notifyNewMessage(
                    message1,
                    authorUser.getNickname(),
                    authorUser.getImageKey(),
                    context
            );

            Awaitility.await()
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> {
                        List<ChatMessageResponse> actual = (List<ChatMessageResponse>) result1.getResult();
                        assertThat(actual).containsExactly(response);
                    });

            @SuppressWarnings("unchecked")
            Map<Long, List<DeferredResult<List<ChatMessageResponse>>>> map =
                    (Map<Long, List<DeferredResult<List<ChatMessageResponse>>>>) ReflectionTestUtils.getField(service, "listeners");
            assertThat(Objects.requireNonNull(map).get(chatRoom.getId())).isEmpty();
        }

        @Test
        @DisplayName("새로운 메세지 발행 성공 - 다중 리스너, 큐 비우기 확인")
        void notify_new_messages_success_multi_listener () {
            when(chatMessageQueryMapper.toMessageResponse(message1, authorUser.getNickname(), authorUser.getImageKey()))
                    .thenReturn(response);

            service.notifyNewMessage(
                    message1,
                    authorUser.getNickname(),
                    authorUser.getImageKey(),
                    context
            );

            Awaitility.await()
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> {
                        List<ChatMessageResponse> actual1 = (List<ChatMessageResponse>) result1.getResult();
                        assertThat(actual1).containsExactly(response);
                        List<ChatMessageResponse> actual2 = (List<ChatMessageResponse>) result2.getResult();
                        assertThat(actual2).containsExactly(response);
                    });

            @SuppressWarnings("unchecked")
            Map<Long, List<DeferredResult<List<ChatMessageResponse>>>> map =
                    (Map<Long, List<DeferredResult<List<ChatMessageResponse>>>>) ReflectionTestUtils.getField(service, "listeners");
            assertThat(Objects.requireNonNull(map).get(chatRoom.getId())).isEmpty();
        }

        @Test
        @DisplayName("새로운 메세지 발행 성공 - 리스너 없음, 큐 비우기 확인")
        void notify_new_messages_success_no_listener () {
            when(chatMessageQueryMapper.toMessageResponse(message1, authorUser.getNickname(), authorUser.getImageKey()))
                    .thenReturn(response);

            service.notifyNewMessage(
                    message1,
                    authorUser.getNickname(),
                    authorUser.getImageKey(),
                    context
            );

            Awaitility.await()
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> {
                        ///  리스너 없음
                    });

            @SuppressWarnings("unchecked")
            Map<Long, List<DeferredResult<List<ChatMessageResponse>>>> map =
                    (Map<Long, List<DeferredResult<List<ChatMessageResponse>>>>) ReflectionTestUtils.getField(service, "listeners");
            assertThat(Objects.requireNonNull(map).get(chatRoom.getId())).isEmpty();
        }

    }
}
