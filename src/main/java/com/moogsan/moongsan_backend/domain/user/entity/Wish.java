package com.moogsan.moongsan_backend.domain.user.entity;

import com.moogsan.moongsan_backend.domain.BaseEntity;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "wish",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wish extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 고유 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;  // 유저 ID, FK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="post_id", nullable = false)
    private GroupBuy groupBuy;  // 공구 ID, FK
}
