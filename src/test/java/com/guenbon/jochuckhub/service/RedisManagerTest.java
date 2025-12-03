package com.guenbon.jochuckhub.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisConnectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration"
})
class RedisManagerTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @SpyBean
    private RedisManager redisManager;

    @Autowired
    private CircuitBreakerRegistry registry;

    @BeforeEach
    void resetCB() {
        registry.circuitBreaker("redisCacheBreaker").reset();
    }

    //    @Test
//    void closed() {
//        final String key = "team:1";
//        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
//        when(valueOps.get(key)).thenReturn("ok");
//
//        for (int i = 0; i < 10; i++) {
//            redisManager.get(key);
//        }
//
//        CircuitBreaker redisCacheBreaker = registry.circuitBreaker("redisCacheBreaker");
//        assertThat(redisCacheBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
//        verify(redisManager, times(10)).get(key);
//        verify(redisManager, never()).fallback(eq(key), any());
//    }
//
    @Test
    void close_to_open() {

        final String key = "team:1";
        CircuitBreaker redisCacheBreaker = registry.circuitBreaker("redisCacheBreaker");

        doThrow(new RuntimeException("RuntimeException occured"))
                .doThrow(new RedisConnectionException("RedisConnectionException occured"))
                .doThrow(new RedisCommandExecutionException("RedisCommandExecutionException occured!"))
                .doReturn(valueOps)
                .doReturn(valueOps)
                .doReturn(valueOps)
                .when(stringRedisTemplate).opsForValue();

        when(valueOps.get(key)).thenReturn("ok");

        for (int i = 0; i < 3; i++) {
            assertThat(redisCacheBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
            redisManager.get(key);
        }

        verify(redisManager, times(3)).get(key);
        // CB 상태에 무관하게 예외 발생 시 fallback 호출됨
        verify(redisManager, times(1)).fallback(eq(key), any(RuntimeException.class));
        verify(redisManager, times(1)).fallback(eq(key), any(RedisConnectionException.class));
        verify(redisManager, times(1)).fallback(eq(key), any(RedisCommandExecutionException.class));

        // 10번 중 5번 이상 fail -> CB 상태 CLOSED -> OPEN 변경 확인
        assertThat(redisCacheBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        for (int i = 0; i < 3; i++) {
            redisManager.get(key);
        }

        // open 상태로 변경 후에는 원본 메서드 호출 x -> 원본 메서드 호출 횟수는 closed 상태의 3회 유지
        verify(redisManager, times(3)).get(key);
        // open 상태에서는 CallNotPermittedException 호출
        verify(redisManager, times(3)).fallback(eq(key), any(CallNotPermittedException.class));
    }
//
//    @Test
//    void open_to_halfOpen() {
//
//        final String key = "team:1";
//        CircuitBreaker redisCacheBreaker = registry.circuitBreaker("redisCacheBreaker");
//
//        when(stringRedisTemplate.opsForValue()).thenThrow(new RuntimeException("redis 조회 시 예외 발생"));
//
//        for (int i = 0; i < 10; i++) {
//            assertThat(redisCacheBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
//            redisManager.get(key);
//        }
//
//        verify(redisManager, times(10)).get(key);
//        // CB 상태에 무관하게 fail 발생 시 fallback 호출됨
//        verify(redisManager, times(10)).fallback(eq(key), any(Throwable.class));
//
//        // 10번 중 5번 이상 fail -> CB 상태 CLOSED -> OPEN 변경 확인
//        assertThat(redisCacheBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
//
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        // 10초 기다린 후 호출하면 HALF_OPEN 상태로 변경
//        for (int i = 0; i < 5; i++) {
//            redisManager.get(key);
//            assertThat(redisCacheBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
//        }
//
//        // 다시 원본 메서드 호출 시도
//        verify(redisManager, times(15)).get(key);
//        // CB 상태에 무관하게 fail 발생 시 fallback 호출됨
//        verify(redisManager, times(15)).fallback(eq(key), any(Throwable.class));
//    }
}