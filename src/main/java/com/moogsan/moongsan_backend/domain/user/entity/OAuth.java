package com.moogsan.moongsan_backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oauth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider", nullable = false)
    private String provider; // "kakao"

    @Column(name = "provider_id", nullable = false)
    private String providerId; // 카카오 고유 ID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token")
    private String refreshToken; // 카카오 리프레시 토큰

    @Column(name = "refresh_token_expires_in", nullable = true)
    private Integer refreshTokenExpiresIn; // 리프레시 토큰 만료까지 남은 초
}
