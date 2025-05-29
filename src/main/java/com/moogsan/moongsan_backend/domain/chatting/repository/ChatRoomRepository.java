package com.moogsan.moongsan_backend.domain.chatting.repository;

import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByGroupBuy_IdAndType(Long postId, String type);

    Optional<ChatRoom> findById(Long Id);
}
