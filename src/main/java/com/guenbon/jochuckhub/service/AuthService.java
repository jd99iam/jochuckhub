package com.guenbon.jochuckhub.service;

import com.guenbon.jochuckhub.dto.JWTReissueDTO;
import com.guenbon.jochuckhub.dto.response.MemberResponseDTO;
import com.guenbon.jochuckhub.exception.JWTException;
import com.guenbon.jochuckhub.exception.errorcode.ErrorCode;
import com.guenbon.jochuckhub.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private final JWTUtil jwtUtil;
    private final MemberService memberService;

    /**
     * SecurityContextHolder에서 username 조회 후
     * Redis에 저장된 refreshToken:{username} 삭제
     */
    public void logout() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String key = "refreshToken:" + username;
        redisTemplate.delete(key);
    }

    public JWTReissueDTO reissue(String refreshToken) {

        if (refreshToken == null) {
            throw new JWTException(ErrorCode.TOKEN_NOT_FOUND);
        }

        // 1) refreshToken 검증 (만료, 위조 → JWTException 발생)
        String username = jwtUtil.getUsername(refreshToken);

        // 2) redis에 저장된 refreshToken 가져오기
        String key = "refreshToken:" + username;
        String cachedRefreshToken = redisTemplate.opsForValue().get(key);

        if (cachedRefreshToken == null) {
            throw new JWTException(ErrorCode.TOKEN_NOT_FOUND);
        }

        if (!refreshToken.equals(cachedRefreshToken)) {
            throw new JWTException(ErrorCode.TOKEN_MISMATCH);
        }

        // 3) 사용자 정보 가져오기
        MemberResponseDTO memberByUsername = memberService.getMemberByUsername(username);

        // 4) 신규 토큰 발급
        String reissuedAccessToken =
                jwtUtil.createAccessToken(username, memberByUsername.getRole().name());

        String reissuedRefreshToken =
                jwtUtil.createRefreshToken(username);

        // 5) 새 refreshToken으로 Redis 값 교체 + TTL 갱신
        long refreshTokenExpireSeconds = jwtUtil.getRefreshTokenExpireSeconds();

        redisTemplate.opsForValue().set(
                key,
                reissuedRefreshToken,
                Duration.ofSeconds(refreshTokenExpireSeconds)
        );

        // 6) 결과 반환
        return new JWTReissueDTO(
                reissuedAccessToken,
                reissuedRefreshToken,
                (int) refreshTokenExpireSeconds
        );
    }
}
