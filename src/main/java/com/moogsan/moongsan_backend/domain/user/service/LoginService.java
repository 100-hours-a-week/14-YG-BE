package com.moogsan.moongsan_backend.domain.user.service;

import com.moogsan.moongsan_backend.domain.user.dto.request.LoginRequest;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.entity.Token;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenRepository refreshTokenRepository;

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        try {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new UserException(UserErrorCode.INVALID_INPUT, "이메일은 필수 입력 값입니다.");
            }

            if (!request.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw new UserException(UserErrorCode.INVALID_INPUT, "올바른 이메일 형식이어야 합니다.");
            }

            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new UserException(UserErrorCode.INVALID_INPUT, "비밀번호는 필수 입력 값입니다.");
            }

            // 이메일 사용자 조회
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND, "이메일이 존재하지 않습니다."));

            // {oauth}로 시작하는 비밀번호는 OAuth용 계정
            if (user.getPassword().startsWith("{oauth}-")) {
                throw new UserException(UserErrorCode.UNAUTHORIZED, "소셜 로그인으로 가입된 계정입니다.");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new UserException(UserErrorCode.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
            }

            user.setLastLoginAt();

            // JWT 토큰 발급
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);
            Long accessTokenExpireAt = jwtUtil.getAccessTokenExpireAt();
            Long refreshTokenExpireMillis = jwtUtil.getRefreshTokenExpireMillis();

            // 기존 리프레시 토큰 DB에서 제거
            refreshTokenRepository.deleteByUserId(user.getId());

            // 새로운 리프레시 토큰 DB에 저장
            Token newToken = new Token(
                    null,
                    user.getId(),
                    refreshToken,
                    LocalDateTime.now().plusSeconds(refreshTokenExpireMillis / 1000)
            );
            refreshTokenRepository.save(newToken);

            // 엑세스 쿠키 설정
            Cookie accessTokenCookie = new Cookie("AccessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge((int) (accessTokenExpireAt / 1000));
            response.addCookie(accessTokenCookie);
            response.addHeader("Set-Cookie", "AccessToken=" + accessToken + "; HttpOnly; Secure; Path=/; SameSite=None");

            // 리프레시 토큰 설정
            Cookie refreshTokenCookie = new Cookie("RefreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge((int) (refreshTokenExpireMillis / 1000));
            response.addCookie(refreshTokenCookie);
            response.addHeader("Set-Cookie", "RefreshToken=" + refreshToken + "; HttpOnly; Secure; Path=/; SameSite=None");

            // 최소 응답 정보 반환
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
}