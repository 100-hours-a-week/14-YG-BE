package com.moogsan.moongsan_backend.domain.user.service;

import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.repository.TokenRepository;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final OrderRepository orderRepository;
    private final GroupBuyRepository groupBuyRepository;

    @Transactional
    public void withdraw(Long userId, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND, "존재하지 않는 사용자입니다."));

        // 공구 참여 또는 주문 참여 시 삭제 불가
        boolean hasActiveOrders = orderRepository.existsByUserIdAndStatusNotIn(userId, List.of("CANCELED", "CONFIRMED"));
        boolean hasActiveGroupBuys = groupBuyRepository.existsGroupBuyByUserIdAndPostStatusNot(userId, "ENDED");

        if (hasActiveOrders || hasActiveGroupBuys) {
            throw new UserException(UserErrorCode.DUPLICATE_VALUE, "진행 중인 공구 또는 주문이 있습니다.");
        }

        // DB에서 유저 삭제
        tokenRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);

        // 액세스 토큰 삭제
        jakarta.servlet.http.Cookie accessTokenCookie = new jakarta.servlet.http.Cookie("AccessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        response.addHeader("Set-Cookie", "AccessToken=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=None");

        // 리프레시 토큰 삭제
        jakarta.servlet.http.Cookie refreshTokenCookie = new jakarta.servlet.http.Cookie("RefreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        response.addHeader("Set-Cookie", "RefreshToken=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=None");
    }
}