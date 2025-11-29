package com.guenbon.jochuckhub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisManager {

    private final StringRedisTemplate redisTemplate;

    // 조회
    public String get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis GET 실패 (key={}, cause={})", key, e.getMessage());
            return null;  // 명시적으로 실패 시 null 반환
        }
    }

    // 저장 + TTL
    public void set(String key, String value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Redis SET+TTL 실패 (key={}, cause={})", key, e.getMessage());
        }
    }

    // 저장 (TTL 없음)
    public void set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis SET 실패 (key={}, cause={})", key, e.getMessage());
        }
    }

    // 삭제
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis DELETE 실패 (key={}, cause={})", key, e.getMessage());
        }
    }
}
