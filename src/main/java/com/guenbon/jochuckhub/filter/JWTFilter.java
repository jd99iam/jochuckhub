package com.guenbon.jochuckhub.filter;


import com.guenbon.jochuckhub.exception.CustomAuthenticationEntryPoint;
import com.guenbon.jochuckhub.exception.JWTException;
import com.guenbon.jochuckhub.exception.errorcode.ErrorCode;
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
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    // Access Token: 1시간
    private static final long ACCESS_TOKEN_EXPIRE_MS = 1000L * 60 * 60;

    // Refresh Token: 1주
    private static final long REFRESH_TOKEN_EXPIRE_MS = 1000L * 60 * 60 * 24 * 7;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1) Authorization 헤더 추출
            String authorization = request.getHeader("Authorization");

            // 헤더가 없으면 다음 필터로 넘김
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authorization.split(" ")[1];

            // 2) 토큰 검증 (예외 발생 가능)
            if (jwtUtil.isExpired(token)) {
                // 만료된 토큰 예외 직접 발생 → EntryPoint로 전달됨
                throw new JWTException(ErrorCode.EXPIRED_TOKEN);
            }

            // 3) 토큰 타입이 access인지 확인
            String tokenType = jwtUtil.getTokenType(token);
            if (!"access".equals(tokenType)) {
                throw new JWTException(ErrorCode.UNSUPPORTED_TOKEN);
            }

            // 4) username, role 추출
            String username = jwtUtil.getUsername(token);  // 파싱 중 오류 시 예외 발생
            String role = jwtUtil.getRole(token);

            // 5) Authentication 생성
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singleton(new SimpleGrantedAuthority(role))
                    );

            // 6) SecurityContextHolder에 저장
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // 7) 다음 필터
            filterChain.doFilter(request, response);

        } catch (JWTException e) {
            // JWTUtil에서 발생한 예외 또는 우리가 throw한 오류 처리

            // request에 메시지 저장 → EntryPoint에서 꺼내 사용
            request.setAttribute("exception", e.getErrorCode().getMessage());

            // AuthenticationEntryPoint로 위임 (ControllerAdvice에서는 잡히지 않음)
            authenticationEntryPoint.commence(request, response, null);
        }
    }
}
