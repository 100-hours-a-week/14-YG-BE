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
import java.util.List;
import java.util.Optional;

import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.ALREADEY_JOINED;
import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.ORDER_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class JoinChatRoom {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    public Long joinChatRoom(User currentUser,Long postId) {

        // 해당 공구가 존재하는지 조회 -> 없으면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        Boolean isHost = groupBuy.getUser().getId().equals(currentUser.getId());

        if (!isHost) {
            // 해당 공구의 주문 테이블에 해당 유저의 주문이 존재하는지 조회 -> 아니면 404
            Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(currentUser.getId(), groupBuy.getId(),
                            List.of("CANCELED", "REFUNDED"))
                    .orElseThrow(() -> new OrderNotFoundException("공구의 참여자만 참가 가능합니다."));
        }

        // 해당 공구의 참여자 채팅방이 존재하는지 조회
        ChatRoom chatRoom = chatRoomRepository
                .findByGroupBuy_IdAndType(groupBuy.getId(), "PARTICIPANT")
                .orElseGet(() -> {
                    // 없으면 새로 생성 -> 동시 생성 방지 필요
                    ChatRoom newRoom = ChatRoom.builder()
                            .groupBuy(groupBuy)
                            .type("PARTICIPANT")
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        // 이미 호스트가 참여중인지 확인 (만약 새로 생성되었으면 당연히 미참여 상태)
        boolean alreadyJoined = chatParticipantRepository
                .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), currentUser.getId());

        if (alreadyJoined) {
            throw new AlreadyJoinedException(ALREADEY_JOINED);
        } else {
            // 호스트를 참여자로 등록
            ChatParticipant participant = ChatParticipant.builder()
                    .chatRoom(chatRoom)
                    .user(currentUser)
                    .joinedAt(LocalDateTime.now())
                    .build();
            chatParticipantRepository.save(participant);

            // ChatRoom 참여자 수 업데이트
            chatRoom.incrementParticipants();
            chatRoomRepository.save(chatRoom);

            groupBuy.setParticipantChatRoom(chatRoom);

            return chatRoom.getId();
        }
    }
}
