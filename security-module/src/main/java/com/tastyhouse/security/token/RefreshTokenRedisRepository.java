package com.tastyhouse.security.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Refresh Token Redis 저장소
 *
 * <p>Key: "{prefix}{username}" → refreshToken 값 (TTL: refreshTokenExpiration)
 * 키 접두사는 사용처(web/admin)가 빈 등록 시 주입한다(예: "rt:", "admin:rt:").
 */
public class RefreshTokenRedisRepository {

    private final StringRedisTemplate redisTemplate;
    private final String prefix;

    public RefreshTokenRedisRepository(StringRedisTemplate redisTemplate, String prefix) {
        this.redisTemplate = redisTemplate;
        this.prefix = prefix;
    }

    public void save(String username, String refreshToken, long ttlMillis) {
        redisTemplate.opsForValue().set(
                prefix + username,
                refreshToken,
                ttlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    public String find(String username) {
        return redisTemplate.opsForValue().get(prefix + username);
    }

    public boolean isInvalid(String username, String refreshToken) {
        return !refreshToken.equals(find(username));
    }

    public void delete(String username) {
        redisTemplate.delete(prefix + username);
    }
}
