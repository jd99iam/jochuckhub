package com.guenbon.jochuckhub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "refreshToken:";   // key prefix

    private String generateKey(Long userId) {
        return PREFIX + userId;
    }

    /**
     * RefreshToken 저장
     * TTL(만료시간)도 함께 설정하는 방식
     */
    public void saveRefreshToken(Long userId, String refreshToken, long ttlSeconds) {
        String key = generateKey(userId);
        redisTemplate.opsForValue().set(key, refreshToken, ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * RefreshToken 조회
     */
    public String getRefreshToken(Long userId) {
        String key = generateKey(userId);
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * RefreshToken 삭제
     */
    public void deleteRefreshToken(Long userId) {
        String key = generateKey(userId);
        redisTemplate.delete(key);
    }
}