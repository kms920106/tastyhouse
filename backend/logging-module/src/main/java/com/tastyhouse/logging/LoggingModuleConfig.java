package com.tastyhouse.logging;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * logging-module의 진입점 설정.
 *
 * <p>API 요청/응답 로깅 필터와 민감 필드 마스킹 빈을 등록한다. 앱은 이 클래스를
 * {@code @Import} 하는 것으로 로깅 모듈 전체를 활성화한다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan("com.tastyhouse.logging")
public class LoggingModuleConfig {
}
