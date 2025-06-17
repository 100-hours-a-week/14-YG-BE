package com.moogsan.moongsan_backend.unit.chatting.service.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.AlreadyJoinedException;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.chatting.service.command.JoinChatRoom;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.exception.specific.OrderNotFoundException;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.*;
import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_EXIST;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JoinChatRoomTest {

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    private JoinChatRoom joinChatRoom;
    private GroupBuy groupBuy;
    private User hostUser;
    private User participantUser;
    private Order order;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {

        hostUser = User.builder().id(1L).build();
        participantUser = User.builder().id(2L).build();
        groupBuy = GroupBuy.builder().id(20L).user(hostUser).build();
        order = Order.builder().id(30L).user(participantUser).build();
        chatRoom = ChatRoom.builder().id(18L).type("PARTICIPANT").build();

        joinChatRoom = new JoinChatRoom(
                groupBuyRepository,
                orderRepository,
                chatRoomRepository,
                chatParticipantRepository
        );
    }

    @Test
    @DisplayName("참여자 채팅방 참여 성공 - 주최자")
    void joinChatRoom_success_host() {
        when(groupBuyRepository.findById(20L)).thenReturn(Optional.of(groupBuy));
        when(chatRoomRepository.findByGroupBuy_IdAndType(20L, "PARTICIPANT")).thenReturn(Optional.of(chatRoom));
        when(chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), hostUser.getId()))
                .thenReturn(false);

        joinChatRoom.joinChatRoom(hostUser, 20L);

        verify(groupBuyRepository, times(1)).findById(20L);
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdAndType(20L, "PARTICIPANT");
        verify(chatParticipantRepository, times(1))
                .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), hostUser.getId());
    }

    @Test
    @DisplayName("참여자 채팅방 참여 성공 - 참가자")
    void joinChatRoom_success_participant() {
        when(groupBuyRepository.findById(20L)).thenReturn(Optional.of(groupBuy));
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(participantUser.getId(), groupBuy.getId(), "CANCELED"))
                .thenReturn(Optional.of(order));
        when(chatRoomRepository.findByGroupBuy_IdAndType(20L, "PARTICIPANT")).thenReturn(Optional.of(chatRoom));
        when(chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId()))
                .thenReturn(false);

        joinChatRoom.joinChatRoom(participantUser, 20L);

        verify(groupBuyRepository, times(1)).findById(20L);
        verify(orderRepository, times(1)).findByUserIdAndGroupBuyIdAndStatusNot(participantUser.getId(), groupBuy.getId(), "CANCELED");
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdAndType(20L, "PARTICIPANT");
        verify(chatParticipantRepository, times(1))
                .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());
    }

    @Test
    @DisplayName("참여자 채팅방 참여 실패 - 존재하지 않는 공구")
    void joinChatRoom_success_group_buy_not_found() {
        when(groupBuyRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> joinChatRoom.joinChatRoom(participantUser, 20L))
                .isInstanceOf(GroupBuyNotFoundException.class)
                .hasMessageContaining(NOT_EXIST);

        verify(groupBuyRepository, times(1)).findById(20L);
        verify(orderRepository, never()).findByUserIdAndGroupBuyIdAndStatusNot(participantUser.getId(), groupBuy.getId(), "CANCELED");
        verify(chatRoomRepository, never()).findByGroupBuy_IdAndType(20L, "PARTICIPANT");
        verify(chatParticipantRepository, never())
                .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());
    }

    @Test
    @DisplayName("참여자 채팅방 참여 실패 - 참여자가 아님")
    void joinChatRoom_success_group_buy_not_participant() {
        when(groupBuyRepository.findById(20L)).thenReturn(Optional.of(groupBuy));
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(participantUser.getId(), groupBuy.getId(), "CANCELED"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> joinChatRoom.joinChatRoom(participantUser, 20L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(ORDER_NOT_FOUND);

        verify(groupBuyRepository, times(1)).findById(20L);
        verify(orderRepository, times(1)).findByUserIdAndGroupBuyIdAndStatusNot(participantUser.getId(), groupBuy.getId(), "CANCELED");
        verify(chatRoomRepository, never()).findByGroupBuy_IdAndType(20L, "PARTICIPANT");
        verify(chatParticipantRepository, never())
                .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());
    }

    @Test
    @DisplayName("참여자 채팅방 참여 실패 - 이미 참여한 채팅방")
    void joinChatRoom_success_group_buy_already_joined() {
        when(groupBuyRepository.findById(20L)).thenReturn(Optional.of(groupBuy));
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(participantUser.getId(), groupBuy.getId(), "CANCELED"))
                .thenReturn(Optional.of(order));
        when(chatRoomRepository.findByGroupBuy_IdAndType(20L, "PARTICIPANT")).thenReturn(Optional.of(chatRoom));
        when(chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> joinChatRoom.joinChatRoom(participantUser, 20L))
                .isInstanceOf(AlreadyJoinedException.class)
                .hasMessageContaining(ALREADEY_JOINED);

        verify(groupBuyRepository, times(1)).findById(20L);
        verify(orderRepository, times(1)).findByUserIdAndGroupBuyIdAndStatusNot(participantUser.getId(), groupBuy.getId(), "CANCELED");
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdAndType(20L, "PARTICIPANT");
        verify(chatParticipantRepository, times(1))
                .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());
    }
}
