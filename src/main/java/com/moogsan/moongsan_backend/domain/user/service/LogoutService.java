package com.moogsan.moongsan_backend.domain.user.service;

import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final UserRepository userRepository;
    private final TokenRepository refreshTokenRepository;

    @Transactional
    public void logout(Long userId, HttpServletResponse response) {
        try {
            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND, "존재하지 않는 사용자입니다."));

            // 로그아웃 시간 기록
            user.setLogoutAt(LocalDateTime.now());

            // 리프레시 토큰 DB에서 삭제
            refreshTokenRepository.deleteByUserId(userId);

            // 엑세스 쿠키 삭제
            Cookie accessTokenCookie = new Cookie("AccessToken", null);
            accessTokenCookie.setMaxAge(0);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            response.addCookie(accessTokenCookie);
            response.addHeader("Set-Cookie", "AccessToken=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=None");

            // 리프레시 토큰 삭제
            Cookie refreshTokenCookie = new Cookie("RefreshToken", null);
            refreshTokenCookie.setMaxAge(0);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            response.addCookie(refreshTokenCookie);
            response.addHeader("Set-Cookie", "RefreshToken=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=None");

        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new UserException(UserErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}