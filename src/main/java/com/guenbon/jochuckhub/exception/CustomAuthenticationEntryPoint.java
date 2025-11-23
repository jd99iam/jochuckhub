package com.guenbon.jochuckhub.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");

        // 필터에서 저장한 에러 메시지
        String errorMessage = (String) request.getAttribute("exception");
        String message = (errorMessage != null) ? errorMessage : "Unauthorized";

        // JSON 응답 생성
        String json = String.format(
                "{ \"statusCode\": %d, \"message\": \"%s\", \"timestamp\": \"%s\" }",
                HttpServletResponse.SC_UNAUTHORIZED,
                escapeJson(message),
                LocalDateTime.now()
        );

        response.getWriter().write(json);

        // 보안 로그 남기기
        securityLogger.warn(
                "JWT authentication failed - message: {}, uri: {}, ip: {}, userAgent: {}",
                message,
                request.getRequestURI(),
                getClientIp(request),
                request.getHeader("User-Agent")
        );
    }

    private String escapeJson(String text) {
        return text.replace("\"", "\\\"");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For"); // 프록시 앞단이 있을 경우
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr(); // 기본 IP
        }
        return ip;
    }
}