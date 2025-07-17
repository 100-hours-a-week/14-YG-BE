package com.moogsan.moongsan_backend.participantchat.application.service.command;

import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatParticipant;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatRoom;
import com.moogsan.moongsan_backend.participantchat.domain.exception.specific.AlreadyJoinedException;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.groupbuy.domain.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.exception.specific.OrderNotFoundException;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.participantchat.domain.message.ResponseMessage.ALREADEY_JOINED;
import static com.moogsan.moongsan_backend.participantchat.domain.message.ResponseMessage.ORDER_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class JoinChatRoom {

    private final GroupBuyRepository groupBuyRepository;
    private final OrderRepository orderRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    public void joinChatRoom(User currentUser, Long postId) {

        Long userId = currentUser.getId();

        // 유효성 검증
        GroupBuy groupBuy = fetchAndValidate(userId, postId);

        // 채팅방 생성
        ChatRoom chatRoom = fetchChatRoom(userId, groupBuy);

        // 채팅방 참여
        enrollParticipant(currentUser, chatRoom, groupBuy);
    }

    private GroupBuy fetchAndValidate(Long userId, Long postId) {
        // 해당 공구가 존재하는지 조회 -> 없으면 404
        return groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);
    }

    private ChatRoom fetchChatRoom(Long userId, GroupBuy groupBuy) {

        boolean isHost = groupBuy.getUser().getId().equals(userId);

        if (!isHost) {
            // 해당 공구의 주문 테이블에 해당 유저의 주문이 존재하는지 조회 -> 아니면 404
            Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNotIn(userId, groupBuy.getId(),
                            List.of("CANCELED", "REFUNDED"))
                    .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));
        }

        // 해당 공구의 참여자 채팅방이 존재하는지 조회
        return chatRoomRepository
                .findByGroupBuy_IdAndType(groupBuy.getId(), "PARTICIPANT")
                .orElseGet(() -> {
                    // 없으면 새로 생성 -> 동시 생성 방지 필요
                    ChatRoom newRoom = ChatRoom.builder()
                            .groupBuy(groupBuy)
                            .type("PARTICIPANT")
                            .build();
                    return chatRoomRepository.save(newRoom);
                });
    }

    private void enrollParticipant(User user, ChatRoom chatRoom, GroupBuy groupBuy) {
        // 이미 호스트가 참여중인지 확인 (만약 새로 생성되었으면 당연히 미참여 상태)
        boolean alreadyJoined = chatParticipantRepository
                .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), user.getId());

        if (alreadyJoined) {
            throw new AlreadyJoinedException(ALREADEY_JOINED);
        } else {
            // 호스트를 참여자로 등록
            ChatParticipant participant = ChatParticipant.builder()
                    .chatRoom(chatRoom)
                    .user(user)
                    .joinedAt(LocalDateTime.now())
                    .build();
            chatParticipantRepository.save(participant);

            // ChatRoom 참여자 수 업데이트
            chatRoom.incrementParticipants();
            chatRoomRepository.save(chatRoom);

            groupBuy.setParticipantChatRoom(chatRoom);
        }
    }
}
