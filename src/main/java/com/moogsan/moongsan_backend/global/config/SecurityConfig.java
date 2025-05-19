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
                        // 에러 핸들러 공개
                        .requestMatchers("/error", "/error/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/group-buys",                   // 메인
                                "/api/group-buys/*"                            // 상세(한 단계 하위)
                        ).permitAll()
                        // 인증 필요
                        .requestMatchers(HttpMethod.GET,
                                "/api/group-buys/user/wishes",       // 위시 리스트 조회
                                "/api/group-buys/user/hosts",                  // 주최 리스트 조회
                                "/api/group-buys/user/participants",           // 참여 리스트 조회
                                "/api/group-buys/*/participants"        // 공구 참여자 조회
                        ).authenticated()
                        .requestMatchers(
                                "/api/users", "/api/users/token", "/api/users/check-nickname", "/api/users/check-email",
                                "/uploads/**", "/api/group-buys/generation/description").permitAll() // 해당 위치 외에는 토큰 적용
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
                "http://localhost:3000",
                "http://localhost:5173",
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