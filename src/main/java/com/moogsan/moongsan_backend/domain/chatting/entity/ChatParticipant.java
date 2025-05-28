package com.moogsan.moongsan_backend.domain.chatting.entity;

import com.moogsan.moongsan_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_participant")
public class ChatParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    private int join_seq_no = 0;

    @Column(nullable = false)
    private LocalDateTime joined_at;

    private LocalDateTime left_at;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    ///  도메인 메서드
    public boolean isActive() {
        return left_at == null;
    }

    public void markLeft() {
        this.left_at = LocalDateTime.now();
    }
}
