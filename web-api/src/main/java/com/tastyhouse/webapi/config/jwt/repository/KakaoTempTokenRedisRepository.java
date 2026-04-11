package com.tastyhouse.webapi.config.jwt.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 카카오 임시 토큰 Redis 저장소
 *
 * Key: "kakao_temp:{tempToken}" → kakaoAccessToken (TTL: 10분)
 * - NEEDS_SIGN_UP / NEEDS_LINKING 응답 시 발급
 * - 회원가입(/signup/kakao) 또는 계정 연동(/link/kakao) 완료 시 삭제 (1회용)
 */
@Repository
@RequiredArgsConstructor
public class KakaoTempTokenRedisRepository {

    private static final String PREFIX = "kakao_temp:";
    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;

    public void save(String kakaoTempToken, String kakaoAccessToken) {
        redisTemplate.opsForValue().set(
            PREFIX + kakaoTempToken,
            kakaoAccessToken,
            TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    public String findKakaoAccessToken(String kakaoTempToken) {
        return redisTemplate.opsForValue().get(PREFIX + kakaoTempToken);
    }

    public void delete(String kakaoTempToken) {
        redisTemplate.delete(PREFIX + kakaoTempToken);
    }
}
