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
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetLatestMessageSse;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetLatestMessages;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.CHAT_ROOM_NOT_FOUND;
import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.NOT_PARTICIPANT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetLatestMessagesSseTest {

    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageQueryMapper chatMessageQueryMapper;

    // FOR getLatestMessagesSse
    private GetLatestMessageSse service;
    private User participantUser;
    private User normalUser;
    private User authorUser;
    private ChatMessageDocument message1;
    private ChatRoom chatRoom;

    // FOR createLongPollingResult - 단순 매핑이기 때문에 테스트하지 않기로 결정 -

    // FOR notifyNewMessageSse
    private SecurityContext context;
    private SseEmitter emitter1;
    private SseEmitter emitter2;
    private List<SseEmitter> list;
    private ChatMessageResponse response;


    @BeforeEach
    void setUp() {

        participantUser = User.builder().id(1L).build();
        normalUser = User.builder().id(2L).build();
        authorUser = User.builder().id(3L).nickname("lucy").imageKey("images/image1").build();
        chatRoom = ChatRoom.builder().id(20L).type("PARTICIPANT").build();
        message1 = ChatMessageDocument.builder().id("64a6f2c21f9b8e4d3a9b1234").chatRoomId(chatRoom.getId()).build();

        emitter1 = spy(new SseEmitter(0L));
        emitter2 = spy(new SseEmitter(0L));

        service = new GetLatestMessageSse(
                chatParticipantRepository,
                chatRoomRepository,
                chatMessageQueryMapper
        );

        context = mock(SecurityContext.class);

        when(chatMessageQueryMapper.toMessageResponse(message1, authorUser.getNickname(), authorUser.getImageKey()))
                .thenReturn(response);

        service.notifyNewMessageSse(
                message1,
                authorUser.getNickname(),
                authorUser.getImageKey(),
                context
        );

    }

    @Nested
    @DisplayName("Describe GetLatestMessages SSE")
    class DescribeGetLatestMessagesSSE {

        @Test
        @DisplayName("최신 메세지 조회 성공 (SSE) - 참여자")
        void get_lastest_message_sse_success_participant() {
            when(chatRoomRepository.findById(20L)).thenReturn(Optional.of(chatRoom));
            when(chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId()))
                    .thenReturn(true);

            service.getLatestMessagesSse(participantUser, 20L);

            verify(chatRoomRepository, times(1)).findById(20L);
            verify(chatParticipantRepository, times(1)).existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());

        }

        @Test
        @DisplayName("최신 메세지 조회 실패 (SSE) - 존재하지 않는 채팅방")
        void get_lastest_message_sse_fail_chat_room_not_exist() {
            when(chatRoomRepository.findById(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getLatestMessagesSse(normalUser, 20L))
                    .isInstanceOf(ChatRoomNotFoundException.class)
                    .hasMessageContaining(CHAT_ROOM_NOT_FOUND);

            verify(chatRoomRepository, times(1)).findById(20L);
        }

        @Test
        @DisplayName("최신 메세지 조회 실패 (SSE) - 참여자가 아님")
        void get_lastest_message_sse_fail_normal_user() {
            when(chatRoomRepository.findById(20L)).thenReturn(Optional.of(chatRoom));
            when(chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), normalUser.getId()))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.getLatestMessagesSse(normalUser, 20L))
                    .isInstanceOf(NotParticipantException.class)
                    .hasMessageContaining(NOT_PARTICIPANT);

            verify(chatRoomRepository, times(1)).findById(20L);
            verify(chatParticipantRepository, times(1)).existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), normalUser.getId());
        }
    }

    @Nested
    @DisplayName("Describe NotifyNewMessage SSE")
    class DescribeNotifyNewMessageSSE {
        ///  비동기 awaitility

        @Test
        @DisplayName("새로운 메세지 발행 성공 - 단일 emitter 존재")
        void notify_new_messages_sse_success_single_listener() {

            ReflectionTestUtils.invokeMethod(service, "registerEmitter", chatRoom.getId(), emitter1);

            ArgumentCaptor<SseEmitter.SseEventBuilder> captor = ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);

            service.notifyNewMessageSse(message1, participantUser.getNickname(), participantUser.getImageKey(), context);

            Awaitility.await()
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> {
                        verify(emitter1, atLeastOnce()).send(captor.capture());
                    });

            List<SseEmitter.SseEventBuilder> sentEvents = captor.getAllValues();
            assertThat(sentEvents).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("새로운 메세지 발행 성공 - 다중 emitter 존재")
        void notify_new_messages_sse_success_multi_listener() {

            ReflectionTestUtils.invokeMethod(service, "registerEmitter", chatRoom.getId(), emitter1);
            ReflectionTestUtils.invokeMethod(service, "registerEmitter", chatRoom.getId(), emitter2);

            ArgumentCaptor<SseEmitter.SseEventBuilder> captor = ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);

            service.notifyNewMessageSse(message1, participantUser.getNickname(), participantUser.getImageKey(), context);

            Awaitility.await()
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> {
                        verify(emitter1, atLeastOnce()).send(captor.capture());

                        verify(emitter2, atLeastOnce()).send(captor.capture());
                    });

            List<SseEmitter.SseEventBuilder> sentEvents = captor.getAllValues();
            assertThat(sentEvents).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("새로운 메세지 발행 성공 - emitter 존재하지 않음")
        void notify_new_messages_sse_success_no_listener() {

            assertDoesNotThrow(() ->
                service.notifyNewMessageSse(
                    message1,
                    authorUser.getNickname(),
                    authorUser.getImageKey(),
                    context
                )
            );

            service.notifyNewMessageSse(message1, participantUser.getNickname(), participantUser.getImageKey(), context);

            @SuppressWarnings("unchecked")
            Map<Long,List<SseEmitter>> emitters =
                    (Map<Long,List<SseEmitter>>) ReflectionTestUtils.getField(service, "emitters");
            assertThat(emitters).doesNotContainKey(chatRoom.getId());
        }
    }

    @Test
    @DisplayName("send 실패 시 completeWithError 호출")
    void notify_new_message_sse_sendError() throws Exception {
        doThrow(new IOException("fail")).when(emitter1).send(any(SseEmitter.SseEventBuilder.class));
        ReflectionTestUtils.invokeMethod(service, "registerEmitter", chatRoom.getId(), emitter1);

        service.notifyNewMessageSse(message1, participantUser.getNickname(), participantUser.getImageKey(), context);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        verify(emitter1).completeWithError(any(IOException.class))
                );
    }
}
