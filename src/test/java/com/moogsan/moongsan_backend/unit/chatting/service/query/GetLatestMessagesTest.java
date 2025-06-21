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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetLatestMessagesTest {

    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private ChatParticipantRepository chatParticipantRepository;
    @Mock private ChatRoomRepository chatRoomRepository;
    @Mock private ChatMessageQueryMapper chatMessageQueryMapper;

    private GetLatestMessages service;
    private User participantUser;
    private User normalUser;
    private User authorUser;
    private ChatRoom chatRoom;
    private String lastMessageId;
    private ChatMessageDocument message1;
    private ChatMessageDocument message2;
    private List<ChatMessageDocument> newMessages;

    private SecurityContext context;
    private DeferredResult<List<ChatMessageResponse>> result1;
    private DeferredResult<List<ChatMessageResponse>> result2;
    private ChatMessageResponse response;

    @BeforeEach
    void setUp() {
        participantUser = User.builder().id(1L).build();
        normalUser    = User.builder().id(2L).build();
        authorUser    = User.builder().id(3L).nickname("lucy").imageKey("images/image1").build();
        chatRoom      = ChatRoom.builder().id(20L).type("PARTICIPANT").build();
        lastMessageId = "64a6f2c21f9b8e4d3a9b1233";
        message1      = ChatMessageDocument.builder().id("64a6f2c21f9b8e4d3a9b1234").chatRoomId(20L).build();
        message2      = ChatMessageDocument.builder().id("64a6f2c21f9b8e4d3a9b1235").chatRoomId(20L).build();
        newMessages   = List.of(message1, message2);

        service = new GetLatestMessages(
                chatMessageRepository,
                chatParticipantRepository,
                chatRoomRepository,
                chatMessageQueryMapper
        );

        result1 = service.createLongPollingResult(participantUser, chatRoom.getId());
        result2 = service.createLongPollingResult(normalUser,   chatRoom.getId());

        response = ChatMessageResponse.builder().build();
        context  = mock(SecurityContext.class);
    }

    @Nested
    @DisplayName("Describe GetLatestMessages")
    class DescribeGetLatestMessages {
        @Test @DisplayName("최신 메세지 조회 성공 (롱폴링) - 참여자")
        void get_lastest_message_success_participant () {
            when(chatRoomRepository.findById(20L)).thenReturn(java.util.Optional.of(chatRoom));
            when(chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(20L, 1L))
                    .thenReturn(true);
            when(chatMessageRepository.findMessagesAfter(20L, lastMessageId))
                    .thenReturn(newMessages);

            service.getLatestMessages(participantUser, 20L, lastMessageId);

            verify(chatRoomRepository, times(1)).findById(20L);
            verify(chatParticipantRepository, times(1))
                    .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(20L, 1L);
            verify(chatMessageRepository, times(1))
                    .findMessagesAfter(20L, lastMessageId);
        }

        @Test @DisplayName("최신 메세지 조회 실패 - 채팅방 없음")
        void get_lastest_message_fail_chat_room_not_exist () {
            when(chatRoomRepository.findById(20L)).thenReturn(java.util.Optional.empty());

            assertThatThrownBy(() ->
                    service.getLatestMessages(normalUser, 20L, lastMessageId)
            ).isInstanceOf(ChatRoomNotFoundException.class)
                    .hasMessageContaining(CHAT_ROOM_NOT_FOUND);

            verify(chatRoomRepository, times(1)).findById(20L);
        }

        @Test @DisplayName("최신 메세지 조회 실패 - 참여자 아님")
        void get_lastest_message_fail_normal_user() {
            when(chatRoomRepository.findById(20L)).thenReturn(java.util.Optional.of(chatRoom));
            when(chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(20L, 2L))
                    .thenReturn(false);

            assertThatThrownBy(() ->
                    service.getLatestMessages(normalUser, 20L, lastMessageId)
            ).isInstanceOf(NotParticipantException.class)
                    .hasMessageContaining(NOT_PARTICIPANT);

            verify(chatParticipantRepository, times(1))
                    .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(20L, 2L);
        }
    }

    @Nested
    @DisplayName("Describe NotifyNewMessage")
    class DescribeNotifyNewMessage {

        @Test
        @DisplayName("단일 리스너에만 정상 전달 후 제거")
        void notify_new_messages_success_single_listener () {
            when(chatMessageQueryMapper.toMessageResponse(message1, authorUser.getNickname(), authorUser.getImageKey()))
                    .thenReturn(response);

            @SuppressWarnings("unchecked")
            Map<Long, Map<Long, DeferredResult<List<ChatMessageResponse>>>> listeners =
                (Map<Long, Map<Long, DeferredResult<List<ChatMessageResponse>>>>) ReflectionTestUtils.getField(service, "listeners");
            listeners.get(chatRoom.getId()).remove(normalUser.getId());

            service.notifyNewMessage(message1, authorUser.getNickname(), authorUser.getImageKey(), context);

            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                // 먼저 null이 아님을 확인
                assertThat(result1.getResult()).isNotNull();
                assertThat((List<ChatMessageResponse>) result1.getResult())
                                .containsExactly(response);
                // 두 번째 리스너는 메시지를 받지 않음
                assertThat(result2.getResult()).isNull();
            });
        }

        @Test
        @DisplayName("다중 리스너에 정상 전달 후 제거")
        void notify_new_messages_success_multi_listener () {
            // 두 개의 리스너를 전부 active 상태로 두었기 때문에
            // 둘 다 메시지를 받아야 함
            when(chatMessageQueryMapper.toMessageResponse(message1, authorUser.getNickname(), authorUser.getImageKey()))
                    .thenReturn(response);

            service.notifyNewMessage(message1, authorUser.getNickname(), authorUser.getImageKey(), context);

            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                assertThat(result1.getResult()).isNotNull();
                assertThat(result2.getResult()).isNotNull();
                assertThat((List<ChatMessageResponse>) result1.getResult())
                                .containsExactly(response);
                assertThat((List<ChatMessageResponse>) result2.getResult())
                                .containsExactly(response);
            });
        }

        @Test
        @DisplayName("리스너가 없을 때도 예외 없이 동작")
        void notify_new_messages_success_no_listener () {
            @SuppressWarnings("unchecked")
            Map<Long, Map<Long, DeferredResult<List<ChatMessageResponse>>>> map =
                    (Map<Long, Map<Long, DeferredResult<List<ChatMessageResponse>>>>)
                            ReflectionTestUtils.getField(service, "listeners");

            map.clear();

            when(chatMessageQueryMapper.toMessageResponse(message1, authorUser.getNickname(), authorUser.getImageKey()))
                    .thenReturn(response);
            service.notifyNewMessage(message1, authorUser.getNickname(), authorUser.getImageKey(), context);

            await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
                    assertThat(map.get(chatRoom.getId())).isNull()
            );
        }
    }
}
