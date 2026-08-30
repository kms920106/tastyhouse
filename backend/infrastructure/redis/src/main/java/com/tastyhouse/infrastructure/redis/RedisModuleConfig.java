package com.tastyhouse.infrastructure.redis;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * infrastructure:redis 모듈의 진입점 설정 — Redis 연결 템플릿과 레이트 리밋.
 *
 * <p>{@link RedisConfig}(StringRedisTemplate)와 {@code ratelimit} 패키지의
 * {@code RateLimiterService}·{@code RateLimitAspect}를 등록한다.
 *
 * <p>Redis를 쓰는 앱(web-api·admin-api·ceo-api)만 {@code @Import} 한다. batch-module은
 * Redis를 사용하지 않으므로 import 하지 않는다.
 *
 * <p>과거에는 이 빈들이 {@code com.tastyhouse.security} 아래에 있어
 * {@code SecurityModuleConfig}의 컴포넌트 스캔에 함께 걸렸다. 모듈 분리 후에는 스캔 범위가
 * 갈라지므로 각 앱이 이 설정을 명시적으로 import 해야 한다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan("com.tastyhouse.infrastructure.redis")
public class RedisModuleConfig {
}
