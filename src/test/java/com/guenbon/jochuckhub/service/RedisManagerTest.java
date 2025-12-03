package com.guenbon.jochuckhub.service;

import com.guenbon.jochuckhub.service.redis.RedisManager;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

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
    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void resetCB() {
        registry.circuitBreaker("redisCacheBreaker").reset();
    }

    @Test
    void slow_call_test_v1() {

        final String key = "team:1";
        CircuitBreaker redisCacheBreaker = registry.circuitBreaker("redisCacheBreaker");

        assertThat(redisCacheBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        System.out.println("CircuitBreaker 상태 : " + redisCacheBreaker.getState());
        // 1. opsForValue() 호출 시 2초 지연 후 valueOps를 반환하도록 설정
        doAnswer(invocation -> {
            Thread.sleep(2000); // 2000ms (2초) 지연
            System.out.println("<<< 2초 지연");
            return valueOps;
        })
                .when(stringRedisTemplate).opsForValue();

        // 2. valueOps.get(key) 호출이 성공적으로 값을 반환하도록 설정
        when(valueOps.get(key)).thenReturn("slow_call_result");

        System.out.println("-----3회 호출 시작-----");
        for (int i = 0; i < 3; i++) {
            System.out.println("state : " + redisCacheBreaker.getState());
            // 3회 호출
            String result = redisManager.get(key);
//            System.out.println("result : " + result);
        }
        System.out.println("-----3회 호출 끝-----");

        System.out.println("CircuitBreaker 상태 : " + redisCacheBreaker.getState());
        assertThat(redisCacheBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void retry_test_v2() {

        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("runtime exception!"));

        // 1회 호출
        redisManager.get("key");


    }
}