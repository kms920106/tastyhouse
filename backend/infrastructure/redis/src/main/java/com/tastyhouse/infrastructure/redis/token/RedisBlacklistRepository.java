package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.BlacklistRepository;

/**
 * {@link BlacklistRepository}의 Redis 구현 — security-core가 소유한 계약을 인프라가 구현한다.
 *
 * <p>Key: {@code "{keyPrefix}bl:{accessToken}"} → "logout" (TTL: 토큰 잔여 만료 시간)
 * 토큰이 만료되면 Redis TTL에 의해 자동 제거되므로 메모리 낭비 없음.
 */
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
