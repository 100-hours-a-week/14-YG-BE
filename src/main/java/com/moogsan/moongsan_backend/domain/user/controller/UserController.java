package com.moogsan.moongsan_backend.domain.user.controller;

import com.moogsan.moongsan_backend.domain.user.dto.request.LoginRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.SignUpRequest;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.CheckDuplicationResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.UserProfileResponse;
import com.moogsan.moongsan_backend.domain.user.service.CheckDuplicationService;
import com.moogsan.moongsan_backend.domain.user.service.LoginService;
import com.moogsan.moongsan_backend.domain.user.service.LogoutService;
import com.moogsan.moongsan_backend.domain.user.service.SignUpService;
import com.moogsan.moongsan_backend.domain.user.service.UserProfileService;
import com.moogsan.moongsan_backend.domain.user.service.WithdrawService;
import com.moogsan.moongsan_backend.domain.user.service.TokenRefreshService;
import com.moogsan.moongsan_backend.domain.user.service.WishService;
import com.moogsan.moongsan_backend.domain.user.service.CheckAccountService;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.domain.WrapperResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final LoginService loginService;
    private final SignUpService signUpService;
    private final UserProfileService userProfileService;
    private final CheckDuplicationService nicknameService;
    private final CheckDuplicationService emailService;
    private final LogoutService logoutService;
    private final WithdrawService withdrawService;
    private final TokenRefreshService tokenRefreshService;
    private final WishService wishService;
    private final CheckAccountService checkAccountService;

    // 회원가입 API
    @PostMapping("/users")
    public ResponseEntity<WrapperResponse<LoginResponse>> signUp(@Valid @RequestBody SignUpRequest request,
                                                                 HttpServletResponse response) {
        LoginResponse loginResponse = signUpService.signUp(request, response);
        return ResponseEntity.ok(
            WrapperResponse.<LoginResponse>builder()
                .message("회원가입이 완료되었습니다.")
                .data(loginResponse)
                .build()
        );
    }

    // 로그인 API
    @PostMapping("/users/token")
    public ResponseEntity<WrapperResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                                jakarta.servlet.http.HttpServletResponse response) {
        LoginResponse loginResponse = loginService.login(loginRequest, response);
        return ResponseEntity.ok(
            WrapperResponse.<LoginResponse>builder()
                .message("로그인이 완료되었습니다.")
                .data(loginResponse)
                .build()
        );
    }

    // 닉네임 중복 체크 API
    @GetMapping("/users/check-nickname")
    public ResponseEntity<WrapperResponse<CheckDuplicationResponse>> checkNickname(@RequestParam String nickname) {
        CheckDuplicationResponse response = nicknameService.checkNickname(nickname);

        if ("YES".equals(response.getIsDuplication())) {
            return ResponseEntity.ok(
                    WrapperResponse.<CheckDuplicationResponse>builder()
                            .message("이미 등록된 닉네임입니다.")
                            .data(response)
                            .build()
            );
        }

        return ResponseEntity.ok(
                WrapperResponse.<CheckDuplicationResponse>builder()
                        .message("사용 가능한 닉네임입니다.")
                        .data(response)
                        .build()
        );
    }

    // 이메일 중복 체크 API
    @GetMapping("/users/check-email")
    public ResponseEntity<WrapperResponse<CheckDuplicationResponse>> checkEmail(@RequestParam String email) {
        CheckDuplicationResponse response = emailService.checkEmail(email);

        if ("YES".equals(response.getIsDuplication())) {
            return ResponseEntity.ok(
                    WrapperResponse.<CheckDuplicationResponse>builder()
                            .message("이미 등록된 이메일입니다.")
                            .data(response)
                            .build()
            );
        }

        return ResponseEntity.ok(
                WrapperResponse.<CheckDuplicationResponse>builder()
                        .message("사용 가능한 이메일입니다.")
                        .data(response)
                        .build()
        );
    }

    // 유저 프로필 조회 API
    @GetMapping("/users/profile")
    public ResponseEntity<WrapperResponse<UserProfileResponse>> getProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        HttpServletResponse response) {

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");

        UserProfileResponse profile = userProfileService.getUserProfile(userDetails.getUser().getId());
        return ResponseEntity.ok(
            WrapperResponse.<UserProfileResponse>builder()
                .message("유저 정보 조회에 성공했습니다")
                .data(profile)
                .build());
    }

    // 로그아웃 API
    @DeleteMapping("/users/token")
    public ResponseEntity<WrapperResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        HttpServletResponse response) {
        logoutService.logout(userDetails.getUser().getId(), response);
        return ResponseEntity.ok(
            WrapperResponse.<Void>builder()
                    .message("로그아웃이 성공적으로 처리되었습니다.")
                    .data(null)
                    .build()
        );
    }

    // 회원탈퇴 API
    @DeleteMapping("/users")
    public ResponseEntity<WrapperResponse<Void>> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          HttpServletResponse response) {
        withdrawService.withdraw(userDetails.getUser().getId(), response);
        return ResponseEntity.ok(
                WrapperResponse.<Void>builder()
                        .message("회원탈퇴가 완료되었습니다.")
                        .data(null)
                        .build()
        );
    }

    // Access Token 재발행 API
    @PostMapping("/users/token/refresh")
    public ResponseEntity<WrapperResponse<Void>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        tokenRefreshService.refreshAccessToken(request, response);
        return ResponseEntity.ok(
                WrapperResponse.<Void>builder()
                        .message("Access Token이 재발급되었습니다.")
                        .data(null)
                        .build()
        );
    }

    // 관심 목록 등록 API
    @PostMapping("/users/wish/{postId}")
    public ResponseEntity<WrapperResponse<Void>> addWish(@PathVariable Long postId,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        wishService.addWish(userDetails.getUser().getId(), postId);

        return ResponseEntity.ok(
                WrapperResponse.<Void>builder()
                        .message("관심 등록이 완료되었습니다.")
                        .data(null)
                        .build()
        );
    }

    // 관심 목록 삭제 API
    @DeleteMapping("/users/wish/{postId}")
    public ResponseEntity<WrapperResponse<Void>> removeWish(@PathVariable Long postId,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        wishService.removeWish(userDetails.getUser().getId(), postId);

        return ResponseEntity.ok(
                WrapperResponse.<Void>builder()
                        .message("관심 등록이 취소되었습니다.")
                        .data(null)
                        .build()
        );
    }

    // 계좌 정보 실명 인증
    @GetMapping("/users/check/account")
    public ResponseEntity<WrapperResponse<Void>> checkAccount(
            @RequestParam String name,
            @RequestParam String accountBank,
            @RequestParam String accountNumber) {
        checkAccountService.checkBankAccountHolder(accountBank, accountNumber, name); // matched 확인, 예외 발생 시 자동 처리
        return ResponseEntity.ok(
                WrapperResponse.<Void>builder()
                        .message("본인인증이 성공하였습니다.")
                        .data(null)
                        .build()
        );
    }
}