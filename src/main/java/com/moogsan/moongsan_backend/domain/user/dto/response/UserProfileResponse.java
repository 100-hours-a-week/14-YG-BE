package com.moogsan.moongsan_backend.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileResponse {
    private String profileImageUrl;    // 프로필 이미지 URL
    private String nickname;    // 닉네임
    private String name;        // 실명
    private String email;       // 이메일
    private String phoneNumber; // 전화번호
    private String accountBank; // 은행명
    private String accountNumber;   // 계좌번호
    private String type;        // 유저유형
}