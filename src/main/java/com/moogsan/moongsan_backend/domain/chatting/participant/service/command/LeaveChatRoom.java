package com.moogsan.moongsan_backend.domain.chatting.participant.service.command;

import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.exception.specific.OrderNotFoundException;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.moogsan.moongsan_backend.domain.chatting.participant.message.ResponseMessage.CHAT_ROOM_NOT_FOUND;
import static com.moogsan.moongsan_backend.domain.chatting.participant.message.ResponseMessage.ORDER_NOT_FOUND;
import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_PARTICIPANT;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LeaveChatRoom {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    public void leaveChatRoom(User currentUser, Long groupBuyId) {

        // 해당 공구의 주문 테이블에 해당 유저의 주문이 존재하는지 조회 -> 아니면 404
        Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(currentUser.getId(), groupBuyId,
                        List.of("CANCELED", "REFUNDED"))
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        // 해당 공구의 참여자 채팅방이 존재하는지 조회
        ChatRoom chatRoom = chatRoomRepository
                .findByGroupBuy_IdAndType(groupBuyId, "PARTICIPANT")
                .orElseThrow(() -> new ChatRoomNotFoundException(CHAT_ROOM_NOT_FOUND));

        // 참여자인지 조회 -> 아니면 403
        ChatParticipant chatParticipant = chatParticipantRepository
                .findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), currentUser.getId())
                .orElseThrow(() -> new NotParticipantException(NOT_PARTICIPANT));

        chatRoom.decrementParticipants();
        chatRoomRepository.save(chatRoom);

        chatParticipant.markLeft();
        chatParticipantRepository.save(chatParticipant);

    }
}
