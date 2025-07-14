package com.moogsan.moongsan_backend.global.security.jwt;

import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse res,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        /* 1) 쿠키에서 AccessToken 추출 */
        String token = Optional.ofNullable(req.getHeaders().get("Cookie"))
                .stream().flatMap(List::stream)
                .flatMap(c -> Arrays.stream(c.split(";")))
                .map(String::trim)
                .filter(c -> c.startsWith("AccessToken="))
                .map(c -> c.substring("AccessToken=".length()))
                .findFirst().orElse(null);

        if (token != null) {
            token = URLDecoder.decode(token, StandardCharsets.UTF_8); // 혹시 URL-encoded 됐을 경우
            if (jwtUtil.validateToken(token)) {

                /* 2) JWT → Authentication */
                Long userId = jwtUtil.getUserIdFromToken(token);
                UserDetails user = userDetailsService.loadUserByUsername(String.valueOf(userId));
                var auth = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

                /* 3) SecurityContext 채우기 */
                SecurityContextHolder.getContext().setAuthentication(auth);

                /* 4) STOMP 세션에도 Principal 주입 */
                attributes.put("SPRING.PRINCIPAL", auth); // ★ 핵심
            }
        }

        boolean isParticipant = req.getURI().getPath().startsWith("/ws/participant");
        attributes.put("AUTH_REQUIRED", isParticipant);   // ★ 한 줄만 추가
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
                               WebSocketHandler wsHandler, @Nullable Exception ex) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            // sessionAttributes 생성 후 Principal 주입
            if (wsHandler instanceof org.springframework.web.socket.messaging.SubProtocolWebSocketHandler handler) {
                // nothing: Spring 6 에선 자동 전파 X -> ChannelInterceptor에서 복원
            }
        }
    }
}
