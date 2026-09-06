package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.NaverTempTokenRepository;

@Component
public class RedisNaverTempTokenRepository implements NaverTempTokenRepository {

    private static final String PREFIX = "naver_temp:";
    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;

    public RedisNaverTempTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String naverTempToken, String naverAccessToken) {
        redisTemplate.opsForValue().set(
            PREFIX + naverTempToken,
            naverAccessToken,
            TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    @Override
    public String findNaverAccessToken(String naverTempToken) {
        return redisTemplate.opsForValue().get(PREFIX + naverTempToken);
    }

    @Override
    public void delete(String naverTempToken) {
        redisTemplate.delete(PREFIX + naverTempToken);
    }
}
