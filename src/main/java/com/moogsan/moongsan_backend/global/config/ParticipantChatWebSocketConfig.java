package com.moogsan.moongsan_backend.global.config;

import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SOCKET_FAIL;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class ParticipantChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/participant")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // 필요하면 제거 가능
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub/participant");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {

                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) return message;

                // 1. 토큰 꺼내기
                String token = accessor.getFirstNativeHeader("Authorization"); // 또는 "token"
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }

                if (token != null && jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
                    accessor.setUser(new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    ));
                } else {
                    throw new AccessDeniedException(SOCKET_FAIL);
                }

                return message;
            }
        });
    }
}
