package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.RefreshTokenRepository;

@Component
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private static final String REFRESH_TOKEN_SUFFIX = "rt:";

    private final StringRedisTemplate redisTemplate;
    private final String prefix;

    public RedisRefreshTokenRepository(StringRedisTemplate redisTemplate, RedisTokenStoreProperties properties) {
        this.redisTemplate = redisTemplate;
        this.prefix = properties.keyPrefix() + REFRESH_TOKEN_SUFFIX;
    }

    @Override
    public void save(String username, String refreshToken, long ttlMillis) {
        redisTemplate.opsForValue().set(
                prefix + username,
                refreshToken,
                ttlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public String find(String username) {
        return redisTemplate.opsForValue().get(prefix + username);
    }

    @Override
    public void delete(String username) {
        redisTemplate.delete(prefix + username);
    }
}
