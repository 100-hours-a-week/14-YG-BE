package com.moogsan.moongsan_backend.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.entity.Token;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode;
import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 RefreshToken 추출
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new UserException(UserErrorCode.UNAUTHORIZED, "Refresh Token이 유효하지 않습니다.");
        }

        // 토큰에서 userId 추출
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // DB의 refreshToken과 일치하는지 확인
        Token storedToken = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND, "저장된 Refresh Token이 없습니다."));

        if (!storedToken.getRefreshToken().equals(refreshToken)) {
            throw new UserException(UserErrorCode.UNAUTHORIZED, "Refresh Token이 일치하지 않습니다.");
        }

        // AccessToken 재발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

        String newAccessToken = jwtUtil.generateAccessToken(user);
        long accessTokenExpireAt = jwtUtil.getAccessTokenExpireAt();

        // 쿠키에 새 AccessToken 설정 (ResponseCookie 사용)
        ResponseCookie accessTokenCookie = ResponseCookie.from("AccessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofMillis(accessTokenExpireAt))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("RefreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}