package com.moogsan.moongsan_backend.domain.user.service;

import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode;
import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.OAuthSignUpInfoResponse;
import com.moogsan.moongsan_backend.domain.user.entity.OAuth;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.repository.OAuthRepository;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.component.KakaoOAuthClient;
import com.moogsan.moongsan_backend.domain.user.dto.response.KakaoTokenResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.KakaoUserInfoResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import com.moogsan.moongsan_backend.domain.user.entity.Token;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final OAuthRepository oauthRepository;
    private final JwtUtil jwtUtil;
    private final TokenRepository refreshTokenRepository;

    @Transactional
    public LoginResponse kakaoLogin(String code, HttpServletResponse response) {
        // 카카오 토큰, 정보 요청
        KakaoTokenResponse tokenResponse;
        KakaoUserInfoResponse kakaoUser;
        try {
            tokenResponse = kakaoOAuthClient.requestAccessToken(code);
            kakaoUser = kakaoOAuthClient.requestUserInfo(tokenResponse.getAccessToken());
        } catch (Exception e) {
            throw new UserException(
                UserErrorCode.INTERNAL_SERVER_ERROR,
                "OAuth 요청 중 오류 발생"
            );
        }

        String email = kakaoUser.getEmail();
        String kakaoId = String.valueOf(kakaoUser.getId());

        // 기존 회원 여부 확인
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            String dummyPassword = "{oauth}-" + java.util.UUID.randomUUID();
            throw new UserException(
                UserErrorCode.SIGNUP_REQUIRED,
                "회원가입이 필요합니다.",
                new OAuthSignUpInfoResponse(email, dummyPassword)
            );
        }
        User user = optionalUser.get();

        // 기존 회원인 경우 OAuth 연동 여부 확인
        boolean isAlreadyLinked = oauthRepository.existsByProviderAndUserId("kakao", user.getId());

        if (!isAlreadyLinked) {
            // 연동되어 있지 않다면 이메일 중복 여부 확인 후 연동
            Optional<OAuth> existingOAuth = oauthRepository.findByProviderAndProviderId("kakao", kakaoId);
            if (existingOAuth.isEmpty()) {
                OAuth oauth = OAuth.builder()
                        .provider("kakao")
                        .providerId(kakaoId)
                        .user(user)
                        .refreshToken(tokenResponse.getRefreshToken())
                        .refreshTokenExpiresIn(tokenResponse.getRefreshTokenExpiresIn())
                        .build();
                oauthRepository.save(oauth);
            }
        }

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
                java.time.LocalDateTime.now().plusSeconds(refreshTokenExpireMillis / 1000)
        );
        refreshTokenRepository.save(newToken);

        // 엑세스 쿠키 설정
        Cookie accessTokenCookie = new Cookie("AccessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (accessTokenExpireAt / 1000));
        response.addHeader("Set-Cookie", "AccessToken=" + accessToken + "; HttpOnly; Secure; Path=/; SameSite=None");

        // 리프레시 토큰 설정
        Cookie refreshTokenCookie = new Cookie("RefreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpireMillis / 1000));
        response.addHeader("Set-Cookie", "RefreshToken=" + refreshToken + "; HttpOnly; Secure; Path=/; SameSite=None");

        return new LoginResponse(
                user.getNickname(),
                user.getName(),
                user.getImageKey(),
                user.getType()
        );
    }
}
