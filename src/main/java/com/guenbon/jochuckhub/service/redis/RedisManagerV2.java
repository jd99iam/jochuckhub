package com.guenbon.jochuckhub.service.redis;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// Retry 만 사용
@Slf4j
@RequiredArgsConstructor
//@Primary
@Component
public class RedisManagerV2 implements RedisManager {
    private final StringRedisTemplate redisTemplate;

    // 조회
    @Retry(name = "redisRetry", fallbackMethod = "fallback")
    public String get(String key) {
        log.info("[원본 메서드] : get 호출");
        return redisTemplate.opsForValue().get(key);
    }

    public String fallback(String key, Exception e) {
        log.error("[FALLBACK] : retry failed : {}", e.getMessage());
        return null;
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
