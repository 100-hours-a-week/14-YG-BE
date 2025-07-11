package com.moogsan.moongsan_backend.domain.user.controller;

import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfileAccountRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfileRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfilePasswordRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfileImageRequest;
import com.moogsan.moongsan_backend.domain.user.service.UpdateProfileService;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.domain.WrapperResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class UpdateProfileController {

    private final UpdateProfileService updateProfileService;

    // 이미지 수정
    @PatchMapping("/image")
    public ResponseEntity<WrapperResponse<Void>> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateProfileImageRequest request) {
        updateProfileService.updateProfileImage(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(
                WrapperResponse.<Void>builder()
                        .message("프로필 이미지가 수정되었습니다.")
                        .data(null)
                        .build()
        );
    }

    // 비밀번호 수정
    @PatchMapping("/password")
    public ResponseEntity<WrapperResponse<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfilePasswordRequest request) {
        updateProfileService.updatePassword(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(
                WrapperResponse.<Void>builder()
                        .message("비밀번호가 수정되었습니다.")
                        .data(null)
                        .build()
        );
    }

    // 계좌정보 수정
    @PatchMapping("/account")
    public ResponseEntity<WrapperResponse<Void>> updateAccountInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileAccountRequest request) {
        updateProfileService.updateAccountInfo(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(
                WrapperResponse.<Void>builder()
                        .message("계좌정보가 수정되었습니다.")
                        .data(null)
                        .build()
        );
    }

    // 기본정보 수정
    @PatchMapping
    public ResponseEntity<WrapperResponse<Void>> updateBasicInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        updateProfileService.updateBasicInfo(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(
                WrapperResponse.<Void>builder()
                        .message("기본 정보가 수정되었습니다.")
                        .data(null)
                        .build()
        );
    }
}
