package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.NaverTempTokenRepository;

/**
 * {@link NaverTempTokenRepository}의 Redis 구현 — security-core가 소유한 계약을 인프라가 구현한다.
 *
 * <p>Key: {@code "naver_temp:{tempToken}"} → naverAccessToken (TTL: 10분)
 * 소셜 임시 토큰은 앱 간 공유되지 않으므로 키 접두사 설정을 받지 않는다.
 */
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
