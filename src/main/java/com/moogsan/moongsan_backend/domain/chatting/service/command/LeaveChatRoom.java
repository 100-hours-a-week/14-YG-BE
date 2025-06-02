package com.moogsan.moongsan_backend.domain.chatting.service.command;

import com.moogsan.moongsan_backend.domain.chatting.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.exception.specific.OrderNotFoundException;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

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
        Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(currentUser.getId(), groupBuyId, "CANCELED")
                .orElseThrow(() -> new OrderNotFoundException("공구의 참여자만 나갈 수 있습니다: ORDER."));

        // 해당 공구의 참여자 채팅방이 존재하는지 조회
        ChatRoom chatRoom = chatRoomRepository
                .findByGroupBuy_IdAndType(groupBuyId, "PARTICIPANT")
                .orElseThrow(() -> new ChatRoomNotFoundException("존재하는 채팅방만 나갈 수 있습니다."));

        // 참여자인지 조회 -> 아니면 403
        ChatParticipant chatParticipant = chatParticipantRepository
                .findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), currentUser.getId())
                .orElseThrow(() -> new NotParticipantException("공구의 참여자만 나갈 수 있습니다: CHATPARTICIPANT"));

        chatRoom.decrementParticipants();
        chatRoomRepository.save(chatRoom);

        chatParticipant.markLeft();
        chatParticipantRepository.save(chatParticipant);

    }
}
