package com.moogsan.moongsan_backend.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String nickname;    // 닉네임
    private String name;        // 실명
    private String imageUrl;    // 프로필 이미지 URL
    private String type;        // 유저 유형 USER/ADMIN
}