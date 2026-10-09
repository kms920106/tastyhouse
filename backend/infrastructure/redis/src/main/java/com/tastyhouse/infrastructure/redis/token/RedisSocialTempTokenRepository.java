package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.SocialTempTokenProvider;
import com.tastyhouse.security.token.SocialTempTokenRepository;

@Component
class RedisSocialTempTokenRepository implements SocialTempTokenRepository {

    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;

    public RedisSocialTempTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(SocialTempTokenProvider provider, String tempToken, String credential) {
        redisTemplate.opsForValue().set(
            key(provider, tempToken),
            credential,
            TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    @Override
    public String findCredential(SocialTempTokenProvider provider, String tempToken) {
        return redisTemplate.opsForValue().get(key(provider, tempToken));
    }

    @Override
    public void delete(SocialTempTokenProvider provider, String tempToken) {
        redisTemplate.delete(key(provider, tempToken));
    }

    private static String key(SocialTempTokenProvider provider, String tempToken) {
        return provider.keyPrefix() + tempToken;
    }
}
