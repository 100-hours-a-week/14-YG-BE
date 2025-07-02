package com.moogsan.moongsan_backend.domain.chatting.participant.repository;

import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByGroupBuy_IdAndType(Long postId, String type);

    List<ChatRoom> findByGroupBuy_IdInAndType(List<Long> groupBuyIds, String type);

    Optional<ChatRoom> findById(Long Id);
}
