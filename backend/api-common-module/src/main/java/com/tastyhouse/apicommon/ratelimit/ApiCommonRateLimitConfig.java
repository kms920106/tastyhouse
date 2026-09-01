package com.tastyhouse.apicommon.ratelimit;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * api-common-module의 rate limit 기능만 등록하는 부분 진입점 설정 — web-api 전용.
 *
 * <p>{@link RateLimitAspect}를 등록한다. 카운터 구현체({@code RedisRateLimitCounter})는
 * {@code RedisModuleConfig}가 계속 등록하므로 여기서는 다루지 않는다.
 *
 * <p><b>web-api만 이 설정을 import 하는 이유</b> — admin-api·ceo-api는 공용 예외 응답 계약을 쓰므로
 * {@link com.tastyhouse.apicommon.ApiCommonConfig}를 import 하고, 그 설정의 스캔 범위
 * ({@code com.tastyhouse.apicommon} 전체)가 이 패키지를 이미 포함한다. 반면 web-api는 자체
 * {@code GlobalExceptionHandler}를 쓰느라 부분 진입점({@code ApiCommonFileConfig})만 import 하므로,
 * aspect가 스캔되지 않아 <b>@RateLimit이 조용히 무시된다</b>. 챕터 02에서 aspect가
 * {@code infrastructure:redis}(=모든 앱이 import 하는 {@code RedisModuleConfig}의 스캔 범위)에서
 * 이 모듈로 올라오며 생긴 구멍이라, 이 부분 설정으로 메운다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan("com.tastyhouse.apicommon.ratelimit")
public class ApiCommonRateLimitConfig {
}
