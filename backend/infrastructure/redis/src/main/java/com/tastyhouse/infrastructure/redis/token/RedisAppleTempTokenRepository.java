package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.AppleTempTokenRepository;

/**
 * {@link AppleTempTokenRepository}의 Redis 구현 — security-core가 소유한 계약을 인프라가 구현한다.
 *
 * <p>Key: {@code "apple_temp:{tempToken}"} → appleIdToken (TTL: 10분)
 * 소셜 임시 토큰은 앱 간 공유되지 않으므로 키 접두사 설정을 받지 않는다.
 */
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
