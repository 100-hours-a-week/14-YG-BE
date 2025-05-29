package com.moogsan.moongsan_backend.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //log.debug("▶ JwtFilter start: {} {}", request.getMethod(), request.getRequestURI());
        String token = resolveToken(request);
        //log.debug("   token={}", token);

        boolean valid = false;
        try {
            valid = (token != null && jwtUtil.validateToken(token));
        } catch (Exception e) {
            //log.debug("    token validation threw: {}", e.getMessage());
        }
        //log.debug("    token valid? {}", valid);

        if (valid) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            //log.debug("    token userId={}", userId);

            try {
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(String.valueOf(userId));
                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
                //log.debug("JwtFilter set Authentication for userId={}", userId);
            } catch (UsernameNotFoundException ex) {
                // DB에 없는 userId인 경우엔 로그만 남기고 익명 상태 유지
                //log.warn("토큰의 userId={} 를 DB에서 찾을 수 없어 익명 처리합니다.", userId);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더 우선 확인
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        // 2. 쿠키에서 AccessToken 확인
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("AccessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        // Exclude POST /api/users from filtering
        return path.equals("/api/users") && method.equalsIgnoreCase("POST");
    }
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;  // ASYNC 디스패치에서도 필터 동작
    }
}