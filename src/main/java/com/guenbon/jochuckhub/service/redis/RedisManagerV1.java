package com.guenbon.jochuckhub.service.redis;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisConnectionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// CircuitBreaker 만 사용
@Slf4j
@RequiredArgsConstructor
@Component
@Primary
public class RedisManagerV1 implements RedisManager {
    private final StringRedisTemplate redisTemplate;

    // 조회
    @CircuitBreaker(name = "redisCacheBreaker", fallbackMethod = "fallback")
    public String get(String key) {
        log.info("[원본 메서드] : get 호출");
        return redisTemplate.opsForValue().get(key);
    }

    public String fallback(String key, RedisConnectionException e) {
        log.error("[FALLBACK] : RedisConnectionException : {}", e.getMessage());
        return null;
    }

    public String fallback(String key, RedisCommandExecutionException e) {
        log.error("[FALLBACK] : RedisCommandExecutionException : {}", e.getMessage());
        return null;
    }

    public String fallback(String key, Exception e) {
        log.error("[FALLBACK] : RuntimeException : {}", e.getMessage());
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
