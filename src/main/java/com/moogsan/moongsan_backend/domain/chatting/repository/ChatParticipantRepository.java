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

    @Query("""
    SELECT cp.chatRoom FROM ChatParticipant cp
    WHERE cp.user.id = :userId
      AND cp.chatRoom.type = 'PARTICIPANT'
      AND cp.leftAt IS NULL
    ORDER BY cp.joinedAt DESC, cp.id DESC
""")
    List<ChatRoom> findInitialChatRooms(@Param("userId") Long userId, Pageable pageable);


    @Query("""
            SELECT cp.chatRoom FROM ChatParticipant cp
            WHERE cp.user.id = :userId
              AND cp.chatRoom.type = 'PARTICIPANT'
              AND cp.leftAt is NULL
              AND (
                    cp.joinedAt < :cursorJoinedAt OR
                    (cp.joinedAt = :cursorJoinedAt AND cp.id < :cursorId)
                    )
            ORDER BY cp.joinedAt DESC, cp.id DESC
    """)
    List<ChatRoom> findActiveParticipantChatRoomsByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("cursorJoinedAt") LocalDateTime cursorJoinedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
            );
}
