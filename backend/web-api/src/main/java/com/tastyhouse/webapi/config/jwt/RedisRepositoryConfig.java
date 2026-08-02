package com.tastyhouse.webapi.config.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.tastyhouse.security.token.BlacklistRedisRepository;
import com.tastyhouse.security.token.RefreshTokenRedisRepository;

/**
 * security-module의 접두사 주입형 Redis 저장소를 web-api 전용 키 접두사로 빈 등록한다.
 */
@Configuration
public class RedisRepositoryConfig {

    @Bean
    public RefreshTokenRedisRepository refreshTokenRedisRepository(StringRedisTemplate redisTemplate) {
        return new RefreshTokenRedisRepository(redisTemplate, "rt:");
    }

    @Bean
    public BlacklistRedisRepository blacklistRedisRepository(StringRedisTemplate redisTemplate) {
        return new BlacklistRedisRepository(redisTemplate, "bl:");
    }
}
