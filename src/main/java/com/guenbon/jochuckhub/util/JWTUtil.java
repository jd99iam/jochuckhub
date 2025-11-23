package com.guenbon.jochuckhub.util;

import com.guenbon.jochuckhub.exception.JWTException;
import com.guenbon.jochuckhub.exception.errorcode.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    // Access Token: 1시간
    private static final long ACCESS_TOKEN_EXPIRE_MS = 1000L * 60 * 60;

    // Refresh Token: 1주
    private static final long REFRESH_TOKEN_EXPIRE_MS = 1000L * 60 * 60 * 24 * 7;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        // JJWT 0.12+ 에 맞춘 SecretKeySpec 생성
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    // ======================
    // Claims 파싱 공통 메서드
    // ======================
    private Claims parseClaims(String token) {

        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JWTException(ErrorCode.EXPIRED_TOKEN);

        } catch (SignatureException e) {
            throw new JWTException(ErrorCode.INVALID_SIGNATURE);

        } catch (MalformedJwtException e) {
            throw new JWTException(ErrorCode.MALFORMED_TOKEN);

        } catch (UnsupportedJwtException e) {
            throw new JWTException(ErrorCode.UNSUPPORTED_TOKEN);

        } catch (IllegalArgumentException e) {
            throw new JWTException(ErrorCode.TOKEN_NOT_FOUND);
        }
    }

    // ======================
    // 조회 메서드들
    // ======================
    public String getUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }

    @Nullable
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class); // refreshToken이면 null 가능
    }

    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class); // access / refresh
    }

    public boolean isExpired(String token) {
        Date exp = parseClaims(token).getExpiration();
        return exp.before(new Date());
    }

    // ======================
    // 생성 공통 메서드
    // ======================
    private String createToken(String username,
                               @Nullable String role,
                               String type,
                               long expiredMs) {

        long now = System.currentTimeMillis();
        var builder = Jwts.builder()
                .claim("username", username)
                .claim("type", type)      // access / refresh 구분
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiredMs));

        if (role != null) {
            builder.claim("role", role); // AccessToken 에서만 사용
        }

        return builder
                .signWith(secretKey)
                .compact();
    }

    // ======================
    // 토큰 발급용 메서드
    // ======================

    /**
     * Access Token 발급 (유효기간 1시간)
     */
    public String createAccessToken(String username, String role) {
        return createToken(username, role, "access", ACCESS_TOKEN_EXPIRE_MS);
    }

    /**
     * Refresh Token 발급 (유효기간 1주)
     * - role 은 넣지 않음
     */
    public String createRefreshToken(String username) {
        return createToken(username, null, "refresh", REFRESH_TOKEN_EXPIRE_MS);
    }

    public long getRefreshTokenExpireSeconds() {
        return REFRESH_TOKEN_EXPIRE_MS / 1000;
    }

}
