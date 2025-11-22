package com.guenbon.jochuckhub.config;

import com.guenbon.jochuckhub.filter.JWTFilter;
import com.guenbon.jochuckhub.filter.LoginFilter;
import com.guenbon.jochuckhub.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;

import static com.guenbon.jochuckhub.entity.Role.*;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final StringRedisTemplate redisTemplate;
    private final JWTUtil jwtUtil;
    // AuthenticationConfiguration 생성자 주입받기
    private final AuthenticationConfiguration authenticationConfiguration;

    // 해시 암호화를 위한 빈 등록
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager 빈 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(MASTER.name()).implies(ADMIN.name())
                .role(ADMIN.name()).implies(MEMBER.name())
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // cors 설정
        http
                .cors((corsCustomizer) -> corsCustomizer.configurationSource((request) -> {
                    CorsConfiguration configuration = new CorsConfiguration();

                    configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); // 허용할 origin 지정
                    configuration.setAllowedMethods(Collections.singletonList("*")); // 허용할 HTTP 메서드 지정
                    configuration.setAllowCredentials(true); // 쿠키,세션, Authorization 헤더 같은 Credential 정보를 포함한 요청 허용
                    configuration.setAllowedHeaders(Collections.singletonList("*")); // 클라이언트가 보낼 수 있는 요청 헤더 지정
                    configuration.setMaxAge(3600L); // Preflight(OPTIONS) 요청 결과를 브라우저가 캐싱하는 시간 , 3600초(1시간) 동안 OPTIONS 요청을 다시 보내지 않아도 됨 → 성능 향상

                    configuration.setExposedHeaders(Collections.singletonList("Authorization")); // 서버의 응답 헤더 중 브라우저가 JS에서 읽을 수 있게 허용할 헤더 목록

                    return configuration;
                }));


        // csrf disable
        http
                .csrf(CsrfConfigurer::disable);

        // form 로그인, http basic 로그인 설정 해제
        http
                .formLogin(FormLoginConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable);

        // 필터 등록 (LoginFilter를 원래 UsernamePasswordAuthenticationFilter 의 위치에 등록)
        // 외에도 addFilterBefore, After 등 메서드가 있다.
        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, redisTemplate), UsernamePasswordAuthenticationFilter.class);

        // JWT 검증 필터 등록
        http
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);


        // 인증 필요한 경로 설정
        http
                .authorizeHttpRequests(auth ->
                        auth
                                // MemberController
                                .requestMatchers(HttpMethod.GET, "/members").hasRole(ADMIN.name())
                                .requestMatchers("/members/**").permitAll()
                                .anyRequest().authenticated());

        // 세션 stateless 설정
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
