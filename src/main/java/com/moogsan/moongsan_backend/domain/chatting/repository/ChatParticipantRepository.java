package com.moogsan.moongsan_backend.domain.chatting.repository;

import com.moogsan.moongsan_backend.domain.chatting.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    boolean existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(Long chatRoomId, Long userId);

    Optional<ChatParticipant> findByChatRoom_IdAndUser_IdAndLeftAtIsNull(Long chatRoomId, Long userId);

    // 초기 페이지(커서 없을 때)
    @Query("""
      SELECT p
        FROM ChatParticipant p
        JOIN FETCH p.chatRoom r
       WHERE p.user.id = :userId
         AND p.leftAt IS NULL
       ORDER BY p.joinedAt DESC, p.id DESC
    """)
    List<ChatParticipant> findInitialParticipants(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // 커서(joinedAt) 기반 다음 페이지
    @Query("""
      SELECT p
        FROM ChatParticipant p
        JOIN FETCH p.chatRoom r
       WHERE p.user.id = :userId
         AND p.leftAt IS NULL
         AND p.joinedAt < :cursorJoinedAt
       ORDER BY p.joinedAt DESC, p.id DESC
    """)
    List<ChatParticipant> findParticipantsAfter(
            @Param("userId") Long userId,
            @Param("cursorJoinedAt") LocalDateTime cursorJoinedAt,
            Pageable pageable
    );
}
