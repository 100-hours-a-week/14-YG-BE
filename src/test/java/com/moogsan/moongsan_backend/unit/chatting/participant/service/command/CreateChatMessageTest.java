package com.moogsan.moongsan_backend.unit.chatting.participant.service.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.chatting.participant.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific.ChatRoomInvalidStateException;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.domain.chatting.participant.mapper.ChatMessageCommandMapper;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatMessageRepository;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.chatting.participant.service.command.CreateChatMessage;
import com.moogsan.moongsan_backend.domain.chatting.participant.service.query.GetLatestMessageSse;
import com.moogsan.moongsan_backend.domain.chatting.participant.service.query.GetLatestMessages;
import com.moogsan.moongsan_backend.domain.chatting.participant.util.MessageSequenceGenerator;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.*;
import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.chatting.participant.message.ResponseMessage.CHAT_ROOM_NOT_FOUND;
import static com.moogsan.moongsan_backend.domain.chatting.participant.message.ResponseMessage.DELETED_CHAT_ROOM;
import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_PARTICIPANT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateChatMessageTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private MessageSequenceGenerator messageSequenceGenerator;

    @Mock
    private ChatMessageCommandMapper chatMessageCommandMapper;

    @Mock
    private GetLatestMessages getLatestMessages;

    @Mock
    private GetLatestMessageSse getLatestMessageSse;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private CreateChatMessage createChatMessage;
    private ChatRoom chatRoom;
    private User participantUser;
    private User normalUser;
    private ChatParticipant chatParticipant;
    private ChatMessageDocument chatMessageDocument;
    private String json;
    private CreateChatMessageRequest request;
    private Clock fixedClock;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {

        chatRoom = spy(ChatRoom.builder().id(20L).build());
        participantUser = User.builder().id(1L).build();
        normalUser = User.builder().id(2L).build();
        chatParticipant = ChatParticipant.builder().id(3L).user(participantUser).build();
        request = CreateChatMessageRequest.builder().messageContent("안녕하세요!").build();
        chatMessageDocument = mock(ChatMessageDocument.class);

        fixedClock = Clock.fixed(
                Instant.parse("2025-06-11T13:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        now = LocalDateTime.now(fixedClock);

        createChatMessage = new CreateChatMessage(
                chatRoomRepository,
                chatParticipantRepository,
                chatMessageRepository,
                messageSequenceGenerator,
                chatMessageCommandMapper,
                getLatestMessages,
                getLatestMessageSse,
                redisTemplate,
                objectMapper,
                fixedClock
        );
    }

    @Test
    @DisplayName("참여자 채팅방 메세지 작성 성공")
    void createChatMessage_success() throws JsonProcessingException{
        when(chatRoomRepository.findById(20L)).thenReturn(Optional.of(chatRoom));
        when(chatParticipantRepository.findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId()))
                .thenReturn(Optional.ofNullable(chatParticipant));
        when(messageSequenceGenerator.getNextMessageSeq(chatRoom.getId())).thenReturn(2L);
        when(chatMessageCommandMapper.toMessageDocument(chatRoom, chatParticipant.getId(), request, 2L))
                .thenReturn(chatMessageDocument);
        when(chatMessageDocument.getId()).thenReturn("64df8cfa34fded0a178ed289");
        json = "{\"id\":\"64df8cfa34fded0a178ed289\",\"content\":\"안녕하세요!\"}";
        when(objectMapper.writeValueAsString(chatMessageDocument)).thenReturn(json);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);

        createChatMessage.createChatMessage(participantUser, request, chatRoom.getId());

        verify(chatRoomRepository, times(1)).findById(20L);
        verify(chatParticipantRepository, times(1))
                .findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());
        verify(messageSequenceGenerator, times(1)).getNextMessageSeq(chatRoom.getId());
        verify(chatMessageCommandMapper, times(1)).toMessageDocument(chatRoom, chatParticipant.getId(), request, 2L);
        verify(redisTemplate.opsForZSet(), times(1)).add(anyString(), anyString(), anyDouble());
        verify(redisTemplate, times(1)).expire(anyString(), eq(Duration.ofHours(1)));
    }

    @Test
    @DisplayName("참여자 채팅방 메세지 작성 실패 - 존재하지 않는 채팅방")
    void createChatMessage_fail_chat_room_not_exist() {
        when(chatRoomRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createChatMessage.createChatMessage(participantUser, request, 20L))
                .isInstanceOf(ChatRoomNotFoundException.class)
                .hasMessageContaining(CHAT_ROOM_NOT_FOUND);

        verify(chatRoomRepository, times(1)).findById(20L);
        verify(chatParticipantRepository, never())
                .findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());
        verify(messageSequenceGenerator, never()).getNextMessageSeq(chatRoom.getId());
        verify(chatMessageCommandMapper, never()).toMessageDocument(chatRoom, chatParticipant.getId(), request, 2L);
    }

    @Test
    @DisplayName("참여자 채팅방 메세지 작성 실패 - 채팅방 삭제됨")
    void createChatMessage_fail_delete_chat_room() {
        when(chatRoomRepository.findById(20L)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getDeletedAt()).thenReturn(now.minusDays(2));

        assertThatThrownBy(() -> createChatMessage.createChatMessage(participantUser, request, 20L))
                .isInstanceOf(ChatRoomInvalidStateException.class)
                .hasMessageContaining(DELETED_CHAT_ROOM);

        verify(chatRoomRepository, times(1)).findById(20L);
        verify(chatParticipantRepository, never())
                .findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());
        verify(messageSequenceGenerator, never()).getNextMessageSeq(chatRoom.getId());
        verify(chatMessageCommandMapper, never()).toMessageDocument(chatRoom, chatParticipant.getId(), request, 2L);
    }

    @Test
    @DisplayName("참여자 채팅방 메세지 작성 실패 - 참여자가 아님")
    void createChatMessage_fail_not_participant() {
        when(chatRoomRepository.findById(20L)).thenReturn(Optional.of(chatRoom));
        when(chatParticipantRepository.findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> createChatMessage.createChatMessage(participantUser, request, 20L))
                .isInstanceOf(NotParticipantException.class)
                .hasMessageContaining(NOT_PARTICIPANT);

        verify(chatRoomRepository, times(1)).findById(20L);
        verify(chatParticipantRepository, times(1))
                .findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());
        verify(messageSequenceGenerator, never()).getNextMessageSeq(chatRoom.getId());
        verify(chatMessageCommandMapper, never()).toMessageDocument(chatRoom, chatParticipant.getId(), request, 2L);
    }

}
