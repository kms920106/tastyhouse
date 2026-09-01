package com.tastyhouse.apicommon;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.tastyhouse.apicommon.file.ApiCommonFileConfig;
import com.tastyhouse.apicommon.ratelimit.ApiCommonRateLimitConfig;

/**
 * api-common-module의 진입점 설정 — 공용 빈 전체.
 *
 * <p>공용 {@code GlobalExceptionHandler}({@code @RestControllerAdvice})를 포함하므로
 * 공용 예외 응답 계약을 그대로 쓰는 앱(admin-api·ceo-api)이 {@code @Import} 한다.
 *
 * <p>web-api는 자체 {@code GlobalExceptionHandler}를 쓰므로 이 설정 대신
 * {@link ApiCommonFileConfig}를 import 한다.
 *
 * <p>{@code ApiCommonFileConfig}·{@link ApiCommonRateLimitConfig}는 이 스캔 범위 안에 있지만
 * web-api 전용 부분 진입점이므로 제외한다. 스캔 대상이 겹쳐(파일·ratelimit 패키지) 동작에는
 * 영향이 없으나, 이 설정을 import 한 앱의 컨텍스트에 쓰이지 않는 설정 빈이 올라오지 않게 한다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
    basePackages = "com.tastyhouse.apicommon",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {ApiCommonFileConfig.class, ApiCommonRateLimitConfig.class}
    )
)
public class ApiCommonConfig {
}
