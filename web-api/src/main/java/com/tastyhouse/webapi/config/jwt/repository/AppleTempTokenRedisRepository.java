package com.tastyhouse.webapi.config.jwt.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * Apple 임시 토큰 Redis 저장소
 *
 * Key: "apple_temp:{tempToken}" → appleIdToken (TTL: 10분)
 * - NEEDS_SIGN_UP / NEEDS_LINKING 응답 시 발급
 * - 회원가입(/signup/apple) 또는 계정 연동(/link/apple) 완료 시 삭제 (1회용)
 *
 * Apple은 UserInfo 엔드포인트가 없으므로 accessToken이 아닌 id_token을 저장한다.
 * id_token은 이미 서버에서 검증 완료된 상태이며, sub/email 재추출 시 재파싱된다.
 */
@Repository
@RequiredArgsConstructor
public class AppleTempTokenRedisRepository {

    private static final String PREFIX = "apple_temp:";
    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;

    public void save(String appleTempToken, String appleIdToken) {
        redisTemplate.opsForValue().set(
            PREFIX + appleTempToken,
            appleIdToken,
            TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    public String findAppleIdToken(String appleTempToken) {
        return redisTemplate.opsForValue().get(PREFIX + appleTempToken);
    }

    public void delete(String appleTempToken) {
        redisTemplate.delete(PREFIX + appleTempToken);
    }
}
