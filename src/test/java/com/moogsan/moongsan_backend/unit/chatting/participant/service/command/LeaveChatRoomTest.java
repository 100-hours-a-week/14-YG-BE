package com.moogsan.moongsan_backend.unit.chatting.participant.service.command;

import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.chatting.participant.service.command.LeaveChatRoom;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
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

import java.util.Optional;
import java.util.List;

import static com.moogsan.moongsan_backend.domain.chatting.participant.message.ResponseMessage.CHAT_ROOM_NOT_FOUND;
import static com.moogsan.moongsan_backend.domain.chatting.participant.message.ResponseMessage.ORDER_NOT_FOUND;
import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_PARTICIPANT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveChatRoomTest {

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    private LeaveChatRoom leaveChatRoom;
    private User participantUser;
    private GroupBuy groupBuy;
    private User normalUser;
    private Order order;
    private ChatRoom chatRoom;
    private ChatParticipant chatParticipant;

    @BeforeEach
    void setUp() {
        participantUser = User.builder().id(1L).build();
        groupBuy = GroupBuy.builder().id(3L).build();
        normalUser = User.builder().id(2L).build();
        order = Order.builder().id(5L).user(participantUser).build();
        chatRoom = ChatRoom.builder().id(20L).type("PARTICIPANT").build();
        chatParticipant = ChatParticipant.builder().id(34L).build();

        leaveChatRoom = new LeaveChatRoom(
                groupBuyRepository,
                orderRepository,
                chatRoomRepository,
                chatParticipantRepository
        );
    }

    @Test
    @DisplayName("참여자 채팅방 나가기 성공 - 참여자")
    void leaveChatRoom_success_host() {
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(participantUser.getId(), groupBuy.getId(), List.of("CANCELED", "REFUNDED")))
                .thenReturn(Optional.of(order));
        when(chatRoomRepository.findByGroupBuy_IdAndType(groupBuy.getId(), "PARTICIPANT"))
                .thenReturn(Optional.of(chatRoom));
        when(chatParticipantRepository.findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId()))
                .thenReturn(Optional.of(chatParticipant));

        leaveChatRoom.leaveChatRoom(participantUser, 3L);

        verify(orderRepository, times(1)).findByUserIdAndGroupBuyIdAndStatusNotIn(participantUser.getId(), groupBuy.getId(), List.of("CANCELED", "REFUNDED"));
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdAndType(groupBuy.getId(), "PARTICIPANT");
        verify(chatParticipantRepository, times(1)).findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());

    }

    @Test
    @DisplayName("참여자 채팅방 나가기 실패 - 공구 참여자가 아님")
    void leaveChatRoom_success_not_group_buy_participant() {
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(normalUser.getId(), groupBuy.getId(), List.of("CANCELED", "REFUNDED")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveChatRoom.leaveChatRoom(normalUser, 3L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(ORDER_NOT_FOUND);

        verify(orderRepository, times(1)).findByUserIdAndGroupBuyIdAndStatusNotIn(normalUser.getId(), groupBuy.getId(), List.of("CANCELED", "REFUNDED"));
        verify(chatRoomRepository, never()).findByGroupBuy_IdAndType(groupBuy.getId(), "PARTICIPANT");
        verify(chatParticipantRepository, never()).findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), normalUser.getId());

    }

    @Test
    @DisplayName("참여자 채팅방 나가기 실패 - 존재하지 않는 참여자 채팅방")
    void leaveChatRoom_success_not_exist_chat_room() {
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(participantUser.getId(), groupBuy.getId(), List.of("CANCELED", "REFUNDED")))
                .thenReturn(Optional.of(order));
        when(chatRoomRepository.findByGroupBuy_IdAndType(groupBuy.getId(), "PARTICIPANT"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveChatRoom.leaveChatRoom(participantUser, 3L))
                .isInstanceOf(ChatRoomNotFoundException.class)
                .hasMessageContaining(CHAT_ROOM_NOT_FOUND);

        verify(orderRepository, times(1)).findByUserIdAndGroupBuyIdAndStatusNotIn(participantUser.getId(), groupBuy.getId(), List.of("CANCELED", "REFUNDED"));
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdAndType(groupBuy.getId(), "PARTICIPANT");
        verify(chatParticipantRepository, never()).findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), participantUser.getId());

    }

    @Test
    @DisplayName("참여자 채팅방 나가기 실패 - 채팅 참여자가 아님")
    void leaveChatRoom_success_not_chat_room_participant() {
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(normalUser.getId(), groupBuy.getId(), List.of("CANCELED", "REFUNDED")))
                .thenReturn(Optional.of(order));
        when(chatRoomRepository.findByGroupBuy_IdAndType(groupBuy.getId(), "PARTICIPANT"))
                .thenReturn(Optional.of(chatRoom));
        when(chatParticipantRepository.findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), normalUser.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveChatRoom.leaveChatRoom(normalUser, 3L))
                .isInstanceOf(NotParticipantException.class)
                .hasMessageContaining(NOT_PARTICIPANT);

        verify(orderRepository, times(1)).findByUserIdAndGroupBuyIdAndStatusNotIn(normalUser.getId(), groupBuy.getId(), List.of("CANCELED", "REFUNDED"));
        verify(chatRoomRepository, times(1)).findByGroupBuy_IdAndType(groupBuy.getId(), "PARTICIPANT");
        verify(chatParticipantRepository, times(1)).findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), normalUser.getId());

    }
}
