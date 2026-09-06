package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.BlacklistRepository;

@Component
public class RedisBlacklistRepository implements BlacklistRepository {

    private static final String BLACKLIST_SUFFIX = "bl:";
    private static final String BLACKLISTED_VALUE = "logout";

    private final StringRedisTemplate redisTemplate;
    private final String prefix;

    public RedisBlacklistRepository(StringRedisTemplate redisTemplate, RedisTokenStoreProperties properties) {
        this.redisTemplate = redisTemplate;
        this.prefix = properties.keyPrefix() + BLACKLIST_SUFFIX;
    }

    @Override
    public void add(String accessToken, long expirationMillis) {
        long remainingMillis = expirationMillis - System.currentTimeMillis();
        if (remainingMillis > 0) {
            redisTemplate.opsForValue().set(
                    prefix + accessToken,
                    BLACKLISTED_VALUE,
                    remainingMillis,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    @Override
    public boolean contains(String accessToken) {
        return redisTemplate.hasKey(prefix + accessToken);
    }
}
