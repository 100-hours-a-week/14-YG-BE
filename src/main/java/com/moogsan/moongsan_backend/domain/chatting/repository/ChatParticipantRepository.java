package com.moogsan.moongsan_backend.domain.chatting.repository;

import com.moogsan.moongsan_backend.domain.chatting.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    boolean existsByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);

    Optional<ChatParticipant> findByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);
}
