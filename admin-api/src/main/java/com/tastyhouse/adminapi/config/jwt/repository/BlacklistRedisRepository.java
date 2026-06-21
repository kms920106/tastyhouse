package com.tastyhouse.adminapi.config.jwt.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 관리자 Access Token 블랙리스트 Redis 저장소
 *
 * Key: "admin:bl:{accessToken}" → "logout" (TTL: 토큰 잔여 만료 시간)
 * 토큰이 만료되면 Redis TTL에 의해 자동 제거되므로 메모리 낭비 없음.
 */
@Repository
@RequiredArgsConstructor
public class BlacklistRedisRepository {

    private static final String PREFIX = "admin:bl:";
    private static final String BLACKLISTED_VALUE = "logout";

    private final StringRedisTemplate redisTemplate;

    public void add(String accessToken, long expirationMillis) {
        long remainingMillis = expirationMillis - System.currentTimeMillis();
        if (remainingMillis > 0) {
            redisTemplate.opsForValue().set(
                    PREFIX + accessToken,
                    BLACKLISTED_VALUE,
                    remainingMillis,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public boolean contains(String accessToken) {
        return redisTemplate.hasKey(PREFIX + accessToken);
    }
}
