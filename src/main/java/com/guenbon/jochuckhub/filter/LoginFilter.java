package com.guenbon.jochuckhub.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guenbon.jochuckhub.dto.CustomUserDetails;
import com.guenbon.jochuckhub.dto.request.LoginDTO;
import com.guenbon.jochuckhub.service.redis.RedisManager;
import com.guenbon.jochuckhub.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

// 로그인 할 때 정상 로그인 정보인지 확인하는 필터
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RedisManager redisManager;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RedisManager redisManager) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.redisManager = redisManager;

        setFilterProcessesUrl("/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {


        try {
            // JSON Body 파싱
            ObjectMapper objectMapper = new ObjectMapper();

            LoginDTO loginRequest =
                    objectMapper.readValue(request.getInputStream(), LoginDTO.class);

            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();

            // UsernamePasswordAuthenticationToken 생성
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // AuthenticationManager 에 전달하여 실제 인증 진행
            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 검증 성공 시 호출되는 메서드
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        CustomUserDetails customUserDetails = (CustomUserDetails) authResult.getPrincipal();
        String username = customUserDetails.getUsername();

        GrantedAuthority grantedAuthority = customUserDetails.getAuthorities().stream().toList().get(0);
        String role = grantedAuthority.getAuthority();

        String accessToken = jwtUtil.createAccessToken(username, role);
        String refreshToken = jwtUtil.createRefreshToken(username);

        // RCF 7235 정의에 따라서 Authorization 헤더를 넣을 때 "Authorization: 타입 인증토큰" 형태로 넣어야 한다.
        response.addHeader("Authorization", "Bearer " + accessToken);

        long refreshTokenExpireSeconds = jwtUtil.getRefreshTokenExpireSeconds();

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setPath("/");
        cookie.setMaxAge((int) refreshTokenExpireSeconds);
        response.addCookie(cookie);
        cookie.setHttpOnly(true);
        // cookie.setSecure(true);

        // Redis에 RefreshToken 저장
        String key = "refreshToken:" + username;

        // redis에 refreshToken 저장
        redisManager.set(
                "refreshToken:" + username,
                refreshToken,
                refreshTokenExpireSeconds
        );
    }

    // 검증 실패 시 호출되는 메서드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        securityLogger.info("로그인 실패 : {}", failed.getMessage());

        // UNAUTHORIZED, 클라이언트에서 재발급 요청하면 됨
        response.setStatus(401);
    }
}
