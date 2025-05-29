package com.moogsan.moongsan_backend.domain.chatting.entity;

import com.moogsan.moongsan_backend.domain.BaseEntity;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_room")
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    @Builder.Default
    private int participantsCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_id", nullable = false)
    private GroupBuy groupBuy;

    ///  도메인 메서드
    public void incrementParticipants()  {
        this.participantsCount++;
    }

    public void decrementParticipants()  {
        if(this.participantsCount > 0) {
            this.participantsCount--;
        }
    }
}
