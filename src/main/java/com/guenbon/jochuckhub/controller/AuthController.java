package com.guenbon.jochuckhub.controller;

import com.guenbon.jochuckhub.dto.JWTReissueDTO;
import com.guenbon.jochuckhub.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그아웃, MEMBER 권한 이상 가능
     * <p>
     * redis에서 refreshToken 삭제
     * cookie에서 refreshToken 삭제
     *
     * @return ResponseEntity 04 NO CONTENT
     */
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {

        // redis 에서 refreshToken 삭제
        authService.logout();

        // refreshToken cookie 삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/"); // 동일해야 삭제됨
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    /**
     * refreshToken 재발급, MEMBER 권한 이상 가능
     * 요청 쿠키의 refreshToken이 만료되지 않았을 시 기존 refreshToken 만료 처리하고 재발급
     * 요청 쿠키의 refreshToken이 만료되었을 경우 재 로그인 필요
     *
     * @param refreshToken
     * @return
     */
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        JWTReissueDTO jwtReissueDTO = authService.reissue(refreshToken);
        response.setHeader("Authorization", "Bearer " + jwtReissueDTO.getAccessToken());

        Cookie cookie = new Cookie("refreshToken", jwtReissueDTO.getRefreshToken());
        cookie.setPath("/");
        cookie.setMaxAge(jwtReissueDTO.getRefreshTokenExpiresInSeconds());
        cookie.setHttpOnly(true);
        // cookie.setSecure(true);

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}
