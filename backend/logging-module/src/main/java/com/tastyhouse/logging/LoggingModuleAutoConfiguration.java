package com.tastyhouse.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * logging-module의 auto-configuration.
 *
 * <p>API 요청/응답 로깅 필터와 민감 필드 마스킹 빈을 등록한다. 이 모듈이 앱의 runtimeClasspath에
 * 있으면 자동으로 활성화되며, 앱은 {@code runtimeOnly}로만 의존한다(4개 앱 전부).
 * 끄려면 {@code spring.autoconfigure.exclude}를 쓴다.
 */
@AutoConfiguration
@ComponentScan("com.tastyhouse.logging")
public class LoggingModuleAutoConfiguration {
}
