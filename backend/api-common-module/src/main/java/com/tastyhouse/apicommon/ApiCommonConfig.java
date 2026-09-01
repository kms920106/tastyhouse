package com.tastyhouse.apicommon;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.tastyhouse.apicommon.ratelimit.ApiCommonRateLimitConfig;

/**
 * api-common-module의 진입점 설정 — 공용 빈 전체.
 *
 * <p>공용 {@code GlobalExceptionHandler}({@code @RestControllerAdvice})를 포함하므로
 * 공용 예외 응답 계약을 그대로 쓰는 앱(admin-api·ceo-api)이 {@code @Import} 한다.
 *
 * <p>web-api는 자체 {@code GlobalExceptionHandler}를 쓰므로 이 설정을 import 하지 않는다
 * ({@code @RestControllerAdvice} 빈이 2개가 되기 때문이다). 과거에는 공용 빈 중
 * {@code FileService}만 등록하려고 부분 진입점 {@code ApiCommonFileConfig}를 import 했으나,
 * 파일 업로드 유스케이스가 앱별 {@code FileUploadCommandService}로 내려가면서 그 설정은 사라졌다.
 *
 * <p>{@link ApiCommonRateLimitConfig}는 이 스캔 범위 안에 있지만 web-api 전용 부분 진입점이므로
 * 제외한다. 스캔 대상이 겹쳐(ratelimit 패키지) 동작에는 영향이 없으나, 이 설정을 import 한 앱의
 * 컨텍스트에 쓰이지 않는 설정 빈이 올라오지 않게 한다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
    basePackages = "com.tastyhouse.apicommon",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {ApiCommonRateLimitConfig.class}
    )
)
public class ApiCommonConfig {
}
