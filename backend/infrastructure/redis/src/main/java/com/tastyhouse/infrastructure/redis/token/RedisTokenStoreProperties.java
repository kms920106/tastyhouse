package com.tastyhouse.infrastructure.redis.token;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 토큰 저장소 어댑터가 소비하는 Redis 키 접두사 설정 — "모듈이 소비하는 설정은 모듈이 소유한다".
 *
 * <p>앱마다 같은 Redis 인스턴스를 공유하므로 접두사로 키 공간을 나눈다.
 * web-api는 기본값 {@code ""}, admin-api는 {@code "admin:"}, ceo-api는 {@code "ceo:"}.
 *
 * <p>접두사에 콜론을 빠뜨려도 예외가 나지 않고 기존 세션만 조용히 무효화되므로
 * 값은 반드시 콜론으로 끝내야 한다.
 */
@ConfigurationProperties(prefix = "security.token-store")
public record RedisTokenStoreProperties(@DefaultValue("") String keyPrefix) {
}
