package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.FacebookTempTokenRepository;

/**
 * {@link FacebookTempTokenRepository}의 Redis 구현 — security-core가 소유한 계약을 인프라가 구현한다.
 *
 * <p>Key: {@code "facebook_temp:{tempToken}"} → facebookAccessToken (TTL: 10분)
 * 소셜 임시 토큰은 앱 간 공유되지 않으므로 키 접두사 설정을 받지 않는다.
 */
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
