package com.moogsan.moongsan_backend.image.domain.entity;

import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageKey;

    private String imageResizedKey;

    @Builder.Default
    @Column(nullable = false)
    private int imageSeqNo = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean thumbnail = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_id", nullable = false)
    private GroupBuy groupBuy;
}
