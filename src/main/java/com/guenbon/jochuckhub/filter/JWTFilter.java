package com.guenbon.jochuckhub.filter;


import com.guenbon.jochuckhub.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// 로그인 후, 클라이언트가 JWT를 포함해서 요청할 경우 JWT를 검증하는 필터
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    // Access Token: 1시간
    private static final long ACCESS_TOKEN_EXPIRE_MS = 1000L * 60 * 60;

    // Refresh Token: 1주
    private static final long REFRESH_TOKEN_EXPIRE_MS = 1000L * 60 * 60 * 24 * 7;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1) Authorization 헤더 추출
        String authorization = request.getHeader("Authorization");

        // 헤더가 없으면 다음 필터로 넘김
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.split(" ")[1];

        // 2) 만료 여부 확인
        if (jwtUtil.isExpired(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3) 토큰 타입이 access인지 확인
        String tokenType = jwtUtil.getTokenType(token);
        if (!"access".equals(tokenType)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4) 토큰에서 username, role 추출
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        // 5) Authentication 생성
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singleton(new SimpleGrantedAuthority(role))
                );

        // 6) SecurityContextHolder에 저장 (ThreadLocal)
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 7) 다음 필터로 이동
        filterChain.doFilter(request, response);
    }
}
