package com.tastyhouse.apicommon.file;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * api-common-module의 파일 기능만 등록하는 부분 진입점 설정 — web-api 전용.
 *
 * <p>web-api는 자체 {@code GlobalExceptionHandler}를 쓴다(검증 실패 메시지 형식이
 * "필드명: 메시지"로 다른 등 응답 계약이 다르다). 공용 {@code exception} 패키지까지 스캔하면
 * {@code @RestControllerAdvice} 빈이 2개가 되므로, 공용 빈 중 {@code FileService}만 등록한다.
 *
 * <p>공용 예외 처리를 함께 쓰는 앱은 이 설정 대신
 * {@link com.tastyhouse.apicommon.ApiCommonConfig}를 import 한다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan("com.tastyhouse.apicommon.file")
public class ApiCommonFileConfig {
}
