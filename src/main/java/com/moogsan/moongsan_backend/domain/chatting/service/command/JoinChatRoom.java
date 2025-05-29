package com.moogsan.moongsan_backend.domain.chatting.service.command;

import com.moogsan.moongsan_backend.domain.chatting.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.AlreadyJoinedException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class JoinChatRoom {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    public void joinChatRoom(User currentUser,Long postId) {

        // 해당 공구가 존재하는지 조회 -> 없으면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구가 OPEN인지 조회, dueDate가 현재 이후인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
                || groupBuy.getDueDate().isBefore(LocalDateTime.now())) {
            throw new GroupBuyInvalidStateException("채팅방 참여는 공구가 열려있는 상태에서만 가능합니다.");
        }

        // 해당 공구의 주문 테이블에 해당 유저의 주문이 존재하는지 조회 -> 아니면 404
        Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(currentUser.getId(), groupBuy.getId(), "CANCELED")
                .orElseThrow(() -> new OrderNotFoundException("공구의 참여자만 참가 가능합니다."));

        // 해당 공구의 참여자 채팅방이 존재하는지 조회
        ChatRoom chatRoom = chatRoomRepository
                .findByGroupBuy_IdAndType(postId, "PARTICIPANT")
                .orElseGet(() -> {
                    // 없으면 새로 생성 -> 동시 생성 방지 필요
                    ChatRoom newRoom = ChatRoom.builder()
                            .groupBuy(groupBuy)
                            .type("PARTICIPANT")
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        // 중복된 참여자인지 조회 -> 409
        boolean isJoined = chatParticipantRepository.existsByChatRoom_IdAndUser_Id(chatRoom.getId(), currentUser.getId());

        if(isJoined) {
            throw new AlreadyJoinedException("이미 참여 중인 참여자 채팅방입니다.");
        }

        // 채팅방 참여
        ChatParticipant chatParticipant = ChatParticipant
                .builder()
                .chatRoom(chatRoom)
                .user(currentUser)
                .joined_at(LocalDateTime.now())
                .build();

        chatParticipantRepository.save(chatParticipant);

        // 채팅방 참여 인원 수 업데이트
        chatRoom.incrementParticipants();
        chatRoomRepository.save(chatRoom);

    }
}
