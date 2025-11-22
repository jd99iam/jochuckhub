package com.guenbon.jochuckhub.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

//    private final AuthService authService;
//
//    /**
//     * 로그아웃, MEMBER 권한 이상 가능
//     * <p>
//     * redis에서 refreshToken 삭제
//     * cookie에서 refreshToken 삭제
//     *
//     * @return 204 NO CONTENT
//     */
//    @DeleteMapping("/logout")
//    public ResponseEntity<Void> logout(HttpServletResponse response) {
//
//        // redis 에서 refreshToken 삭제
//        authService.logout();
//
//        // refreshToken cookie 삭제
//        Cookie cookie = new Cookie("refreshToken", null);
//        cookie.setPath("/"); // 동일해야 삭제됨
//        cookie.setMaxAge(0);
//        response.addCookie(cookie);
//
//        return ResponseEntity.noContent().build();
//    }
//
//    // reissue - accessToken 재발급
//    @PostMapping("/reissue")
//    public ResponseEntity<?> reissue() {
//
//    }
}
