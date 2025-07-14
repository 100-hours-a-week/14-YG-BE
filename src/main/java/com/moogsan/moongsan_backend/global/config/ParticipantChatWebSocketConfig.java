package com.moogsan.moongsan_backend.global.config;

import com.moogsan.moongsan_backend.global.security.jwt.JwtHandshakeInterceptor;
import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@Order(1)
public class ParticipantChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    /* ── 엔드포인트 ── */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry r){
        r.addEndpoint("/ws/participant")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor)  // JWT + AUTH_REQUIRED 플래그
                .withSockJS()
                .setSessionCookieNeeded(true);
    }

    /* ── 브로커 ── (추가 prefix만) */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry r){
        r.enableSimpleBroker("/sub");   // /topic 는 이미 등록됨
    }

    /* ── 인증 인터셉터 ── */
    @Override
    public void configureClientInboundChannel(ChannelRegistration r){
        r.interceptors(new ChannelInterceptor(){
            @Override
            public Message<?> preSend(Message<?> msg, MessageChannel ch){

                var acc = StompHeaderAccessor.wrap(msg);

                // 익명 세션이면 바로 통과
                if (!Boolean.TRUE.equals(acc.getSessionAttributes().get("AUTH_REQUIRED")))
                    return msg;

                // Principal 없으면 Handshake 에서 만든 인증 삽입
                if (acc.getUser() == null){
                    var ctx = SecurityContextHolder.getContext().getAuthentication();
                    if (ctx != null && ctx.isAuthenticated())
                        acc.setUser(ctx);
                }
                return msg;   // ★ 원본 그대로! (헤더 보존)
            }
        });
    }
}
