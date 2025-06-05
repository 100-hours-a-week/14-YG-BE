package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.moogsan.moongsan_backend.domain.chatting.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyCommandMapper;
import com.moogsan.moongsan_backend.domain.image.mapper.ImageMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.global.exception.specific.DuplicateRequestException;
import com.moogsan.moongsan_backend.global.lock.DuplicateRequestPreventer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateGroupBuy {
    private final GroupBuyRepository groupBuyRepository;
    private final ImageMapper imageMapper;
    private final GroupBuyCommandMapper groupBuyCommandMapper;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final DuplicateRequestPreventer duplicateRequestPreventer;

    /// 공구 게시글 작성
    public Long createGroupBuy(User currentUser, CreateGroupBuyRequest createGroupBuyRequest) {

        int total = createGroupBuyRequest.getTotalAmount();
        int unit  = createGroupBuyRequest.getUnitAmount();

        Long userId = currentUser.getId();
        String key = "group-buy:creating:" + userId;

        if (!duplicateRequestPreventer.tryAcquireLock(key, 3)) {
            throw new DuplicateRequestException();
        }

        if (unit == 0 || total % unit != 0) {
            throw new GroupBuyInvalidStateException("상품 주문 단위는 상품 전체 수량의 약수여야 합니다.");
        }

        GroupBuy gb = groupBuyCommandMapper.create(createGroupBuyRequest, currentUser);
        imageMapper.mapImagesToGroupBuy(createGroupBuyRequest.getImageKeys(), gb);
        gb.increaseParticipantCount();
        groupBuyRepository.save(gb);

        ChatRoom chatRoom = chatRoomRepository
                .findByGroupBuy_IdAndType(gb.getId(), "PARTICIPANT")
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .groupBuy(gb)
                            .type("PARTICIPANT")
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        // 이미 호스트가 참여중인지 확인 (만약 새로 생성되었으면 당연히 미참여 상태)
        boolean alreadyJoined = chatParticipantRepository
                .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), currentUser.getId());

        if (!alreadyJoined) {
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
        }

        return gb.getId();
    }
}
