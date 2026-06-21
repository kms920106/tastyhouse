package com.tastyhouse.adminapi.config.jwt.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 관리자 Refresh Token Redis 저장소
 *
 * Key: "admin:rt:{username}" → refreshToken 값 (TTL: refreshTokenExpiration)
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private static final String PREFIX = "admin:rt:";

    private final StringRedisTemplate redisTemplate;

    public void save(String username, String refreshToken, long ttlMillis) {
        redisTemplate.opsForValue().set(
                PREFIX + username,
                refreshToken,
                ttlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    public String find(String username) {
        return redisTemplate.opsForValue().get(PREFIX + username);
    }

    public boolean isValid(String username, String refreshToken) {
        return refreshToken.equals(find(username));
    }

    public void delete(String username) {
        redisTemplate.delete(PREFIX + username);
    }
}
