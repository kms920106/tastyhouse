package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.FacebookTempTokenRepository;

@Component
public class RedisFacebookTempTokenRepository implements FacebookTempTokenRepository {

    private static final String PREFIX = "facebook_temp:";
    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;

    public RedisFacebookTempTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String facebookTempToken, String facebookAccessToken) {
        redisTemplate.opsForValue().set(
            PREFIX + facebookTempToken,
            facebookAccessToken,
            TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    @Override
    public String findFacebookAccessToken(String facebookTempToken) {
        return redisTemplate.opsForValue().get(PREFIX + facebookTempToken);
    }

    @Override
    public void delete(String facebookTempToken) {
        redisTemplate.delete(PREFIX + facebookTempToken);
    }
}
