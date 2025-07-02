package com.moogsan.moongsan_backend.global.config;

import com.moogsan.moongsan_backend.global.security.jwt.JwtAuthenticationFilter;
import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\": \"로그인이 필요합니다.\", \"data\": null}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/static/**",
                    "/error", "/error/**",
                    "/api/users",                           // 회원가입
                    "/api/users/token",                     // 로그인
                    "/api/users/check-nickname",            // 닉네임 중복 확인
                    "/api/users/check-email",               // 이메일 중복 확인
                    "/uploads/**",                          // 이미지 업로드
                    "/api/group-buys/generation/description", // AI 응답 생성
                    "/api/users/check/account",             // 계좌 예금주 확인
                    "/kakao/callback",                      // 카카오 OAuth Callback Redirect URI
                    "/api/oauth/kakao/callback/complete",   // OAuth 연동
                    "/ws/chat",                            // WebSocket 핸드셰이크 직접 허용
                    "/ws/chat/**",                          // STOMP WebSocket 연결 허용
                    "/test-chat.html",                      // WebSocket 테스트 HTML
                    "/favicon.ico"                          // 브라우저 요청 아이콘
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/group-buys",                      // 공구글 목록 조회
                    "/api/group-buys/*"                     // 공구글 상세 조회
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/group-buys/user/wishes",          // 위시 리스트 조회
                    "/api/group-buys/user/hosts",           // 주최 리스트 조회
                    "/api/group-buys/user/participants",    // 참여 리스트 조회
                    "/api/group-buys/*/participants"        // 공구 참여자 조회
                ).authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil, userDetailsService),
                AnonymousAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://test.moongsan.com",
                "https://dev.moongsan.com",
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:8080",
                "http://localhost:63342",
                "https://moongsan.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}