package com.moogsan.moongsan_backend.global.config;

import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(0)
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 익명 채팅방용 엔드포인트
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins(
                        "https://test.moongsan.com",
                        "https://dev.moongsan.com",
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "http://localhost:8080",
                        "http://localhost:63342",
                        "https://moongsan.com"
                )
                .setHandshakeHandler(new DefaultHandshakeHandler())
                .withSockJS()
                .setSessionCookieNeeded(true);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic/roomId 형식으로 메시지 브로드캐스트
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // 익명 채팅이므로 인증은 생략
                return message;
            }
        });
    }
}