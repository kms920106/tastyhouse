package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.AppleTempTokenRepository;

@Component
public class RedisAppleTempTokenRepository implements AppleTempTokenRepository {

    private static final String PREFIX = "apple_temp:";
    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;

    public RedisAppleTempTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String appleTempToken, String appleIdToken) {
        redisTemplate.opsForValue().set(
            PREFIX + appleTempToken,
            appleIdToken,
            TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    @Override
    public String findAppleIdToken(String appleTempToken) {
        return redisTemplate.opsForValue().get(PREFIX + appleTempToken);
    }

    @Override
    public void delete(String appleTempToken) {
        redisTemplate.delete(PREFIX + appleTempToken);
    }
}
