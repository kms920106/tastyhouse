package com.tastyhouse.adminapi.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.adminapplication.auth.token.AdminJwtTokenProvider;
import com.tastyhouse.security.jwt.JwtAuthenticationFilter;
import com.tastyhouse.security.token.BlacklistRedisRepository;

/**
 * security-module의 접두사 주입형/파라미터형 공용 JWT 컴포넌트를 admin-api 전용 협력자로 빈 등록한다.
 * (JwtTokenProvider는 admin 전용 하위 클래스가 @Component로 자기 등록한다.)
 */
@Configuration
public class JwtConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
        AdminJwtTokenProvider jwtTokenProvider,
        BlacklistRedisRepository blacklistRepository,
        ObjectMapper objectMapper
    ) {
        return new JwtAuthenticationFilter(jwtTokenProvider, blacklistRepository, objectMapper);
    }
}
