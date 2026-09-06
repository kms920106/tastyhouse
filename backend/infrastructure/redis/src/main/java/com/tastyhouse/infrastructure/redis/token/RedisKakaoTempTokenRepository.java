package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.KakaoTempTokenRepository;

@Component
public class RedisKakaoTempTokenRepository implements KakaoTempTokenRepository {

    private static final String PREFIX = "kakao_temp:";
    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;

    public RedisKakaoTempTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String kakaoTempToken, String kakaoAccessToken) {
        redisTemplate.opsForValue().set(
            PREFIX + kakaoTempToken,
            kakaoAccessToken,
            TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    @Override
    public String findKakaoAccessToken(String kakaoTempToken) {
        return redisTemplate.opsForValue().get(PREFIX + kakaoTempToken);
    }

    @Override
    public void delete(String kakaoTempToken) {
        redisTemplate.delete(PREFIX + kakaoTempToken);
    }
}
