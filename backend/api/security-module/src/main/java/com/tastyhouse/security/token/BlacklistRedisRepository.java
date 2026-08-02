package com.tastyhouse.security.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Access Token 블랙리스트 Redis 저장소
 *
 * <p>Key: "{prefix}{accessToken}" → "logout" (TTL: 토큰 잔여 만료 시간)
 * 토큰이 만료되면 Redis TTL에 의해 자동 제거되므로 메모리 낭비 없음.
 * 키 접두사는 사용처(web/admin)가 빈 등록 시 주입한다(예: "bl:", "admin:bl:").
 */
public class BlacklistRedisRepository {

    private static final String BLACKLISTED_VALUE = "logout";

    private final StringRedisTemplate redisTemplate;
    private final String prefix;

    public BlacklistRedisRepository(StringRedisTemplate redisTemplate, String prefix) {
        this.redisTemplate = redisTemplate;
        this.prefix = prefix;
    }

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

    public boolean contains(String accessToken) {
        return redisTemplate.hasKey(prefix + accessToken);
    }
}
