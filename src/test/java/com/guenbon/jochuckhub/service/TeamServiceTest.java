package com.guenbon.jochuckhub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guenbon.jochuckhub.repository.MemberRepository;
import com.guenbon.jochuckhub.repository.MemberTeamRepository;
import com.guenbon.jochuckhub.repository.TeamRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TeamServiceCircuitBreakerIntegrationTest {

    @Autowired
    private TeamService teamService;

    @MockBean
    private TeamRepository teamRepository;
    @MockBean
    private MemberRepository memberRepository;
    @MockBean
    private MemberTeamRepository memberTeamRepository;
    @MockBean
    private RedisManager redisManager;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CircuitBreakerRegistry cbRegistry;

    CircuitBreaker cb;

    @BeforeEach
    void setUp() {
        cb = cbRegistry.circuitBreaker("redisCacheBreaker");
        cb.reset(); // 테스트마다 초기화
    }


    // ---------------------------------------------------
    // ✔ 1. 정상 호출 (Closed 상태)
    // ---------------------------------------------------
    @Test
    void getCachedTeam_success_flow() {

        String redisKey = "team:1";
        when(redisManager.get(redisKey)).thenReturn("{\"teamName\":\"Arsenal\"}");

        String result = teamService.getCachedTeam(redisKey);

        assertThat(result).isEqualTo("{\"teamName\":\"Arsenal\"}");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        verify(redisManager, times(1)).get(redisKey);
    }

    // ---------------------------------------------------
    // ✔ 2. 슬라이딩 윈도우 10개 중 5번 이상 실패 → OPEN 전환
    // ---------------------------------------------------
    @Test
    void getCachedTeam_circuitBreaker_shouldOpen_afterFailures() {

        String redisKey = "team:1";

        // Redis 호출시 항상 exception 발생
        when(redisManager.get(redisKey)).thenThrow(new RuntimeException("Redis down"));

        // sliding-window-size = 10
        // → 10번 중 5번 이상 실패해야 failure-rate-threshold(50%) 도달
        for (int i = 0; i < 10; i++) {
            teamService.getCachedTeam(redisKey); // fallback 발생
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // 10번 모두 redis 호출 시도
        verify(redisManager, times(10)).get(redisKey);
    }

    // ---------------------------------------------------
    // ✔ 3. OPEN 상태에서는 Redis 호출 없이 즉시 fallback
    // ---------------------------------------------------
    @Test
    void getCachedTeam_shouldFallbackImmediately_whenOpen() {

        String redisKey = "team:1";

        // Redis 실패하도록 설정
        when(redisManager.get(redisKey)).thenThrow(new RuntimeException("Redis down"));

        // failure-rate > 50% 되도록 10번 호출 → OPEN됨
        for (int i = 0; i < 10; i++) {
            teamService.getCachedTeam(redisKey);
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // OPEN 상태에서 다시 호출
        String result = teamService.getCachedTeam(redisKey);

        assertThat(result).isNull();  // fallback

        // OPEN 상태에서는 RedisManager.get() 호출되면 안됨
        verify(redisManager, times(10)).get(redisKey); // total 10번으로 유지
    }
}