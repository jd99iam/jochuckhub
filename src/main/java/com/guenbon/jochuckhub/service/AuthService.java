package com.guenbon.jochuckhub.service;

import com.guenbon.jochuckhub.dto.JWTReissueDTO;
import com.guenbon.jochuckhub.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private final JWTUtil jwtUtil;

    /**
     * SecurityContextHolder에서 username 조회 후
     * Redis에 저장된 refreshToken:{username} 삭제
     */
    public void logout() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String key = "refreshToken:" + username;
        redisTemplate.delete(key);
    }

    /**
     * @param refreshToken
     * @return
     */
    public JWTReissueDTO reissue(String refreshToken) {
        String username = jwtUtil.getUsername(refreshToken);

    }
}
