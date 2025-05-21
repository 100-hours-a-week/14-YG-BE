package com.moogsan.moongsan_backend.domain.user.service;

import com.moogsan.moongsan_backend.domain.user.dto.request.SignUpRequest;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.entity.Token;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화기
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse signUp(SignUpRequest request, HttpServletResponse response) {
        try {
            validateInput(request);
            validateDuplicateUser(request);

            // 회원정보 DB에 저장
            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .nickname(request.getNickname())
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .accountBank(request.getAccountBank())
                    .accountNumber(request.getAccountNumber())
                    .imageKey(request.getImageUrl())
                    .type("USER")
                    .status("ACTIVE")
                    .joinedAt(java.time.LocalDateTime.now())
                    .build();

            User savedUser = userRepository.save(user);
            userRepository.flush();
            String accessToken = jwtUtil.generateAccessToken(savedUser);
            String refreshToken = jwtUtil.generateRefreshToken(savedUser);
            Long accessTokenExpireAt = jwtUtil.getAccessTokenExpireAt();

            // 액세스 토큰 쿠키로 설정
            Cookie accessTokenCookie = new Cookie("AccessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge((int) (accessTokenExpireAt / 1000));
            response.addHeader("Set-Cookie", "AccessToken=" + accessToken + "; HttpOnly; Secure; Path=/; SameSite=None");

            // 리프레시 토큰 쿠키로 설정
            Cookie refreshTokenCookie = new Cookie("RefreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge((int) (jwtUtil.getRefreshTokenExpireMillis() / 1000));
            response.addHeader("Set-Cookie", "RefreshToken=" + refreshToken + "; HttpOnly; Secure; Path=/; SameSite=None");

            // 리프레시 토큰을 DB에 저장
            Token newToken = new Token(
                null,
                savedUser.getId(),
                refreshToken,
                LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpireMillis() / 1000)
            );
            tokenRepository.save(newToken);

            return new LoginResponse(
                    user.getNickname(),
                    user.getName(),
                    user.getImageKey(),
                    user.getType()
            );
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new UserException(UserErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateInput(SignUpRequest request) {
        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new UserException(UserErrorCode.INVALID_INPUT, "이메일의 형식이 올바르지 않습니다.");
        }

        if (request.getPassword() == null || !request.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^*+=-])[A-Za-z\\d!@#$%^*+=-]{8,30}$")) {
            throw new UserException(UserErrorCode.INVALID_INPUT, "비밀번호가 올바르지 않습니다.\n(숫자와 영어, 특수문자로 이루어진 8자 이상, 30자 이하의 문자열,\n특수 문자(!@#$%^*+=-) 한개 이상 입력)");
        }

        if (request.getNickname() == null || request.getNickname().length() < 2 || request.getNickname().length() > 12) {
            throw new UserException(UserErrorCode.INVALID_INPUT, "닉네임이 올바르지 않습니다.\n2자 이상 12자 이하의 닉네임을 입력해주세요");
        }

        if (request.getName() == null || request.getName().length() < 2 || request.getName().length() > 50) {
            throw new UserException(UserErrorCode.INVALID_INPUT, "실명이 올바르지 않습니다.\n2자 이상 50자 이하의 닉네임을 입력해주세요");
        }

        if (request.getPhoneNumber() == null || !request.getPhoneNumber().matches("^\\d{10,11}$")) {
            throw new UserException(UserErrorCode.INVALID_INPUT, "전화번호가 올바르지 않습니다.\n‘-’을 제외한 숫자만 입력, 최대 11글자 입력 가능합니다.");
        }

        if (request.getAccountBank() == null || request.getAccountBank().isBlank()) {
            throw new UserException(UserErrorCode.INVALID_INPUT, "은행명이 올바르지 않습니다.");
        }

        if (request.getAccountNumber() == null || !request.getAccountNumber().matches("^\\d+$")) {
            throw new UserException(UserErrorCode.INVALID_INPUT, "계좌번호가 올바르지 않습니다.\n하이픈(-) 제거한 숫자만 입력해주세요.");
        }
    }

    private void validateDuplicateUser(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserException(UserErrorCode.DUPLICATE_VALUE, "이미 등록된 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new UserException(UserErrorCode.DUPLICATE_VALUE, "이미 등록된 닉네임입니다.");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserException(UserErrorCode.DUPLICATE_VALUE, "이미 등록된 전화번호입니다.");
        }
    }
}