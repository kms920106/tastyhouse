package com.tastyhouse.webapi.config.jwt.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 페이스북 임시 토큰 Redis 저장소
 *
 * Key: "facebook_temp:{tempToken}" → facebookAccessToken (TTL: 10분)
 * - NEEDS_SIGN_UP / NEEDS_LINKING 응답 시 발급
 * - 회원가입(/signup/facebook) 또는 계정 연동(/link/facebook) 완료 시 삭제 (1회용)
 */
@Repository
@RequiredArgsConstructor
public class FacebookTempTokenRedisRepository {

    private static final String PREFIX = "facebook_temp:";
    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;

    public void save(String facebookTempToken, String facebookAccessToken) {
        redisTemplate.opsForValue().set(
            PREFIX + facebookTempToken,
            facebookAccessToken,
            TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    public String findFacebookAccessToken(String facebookTempToken) {
        return redisTemplate.opsForValue().get(PREFIX + facebookTempToken);
    }

    public void delete(String facebookTempToken) {
        redisTemplate.delete(PREFIX + facebookTempToken);
    }
}
