package com.tastyhouse.infrastructure;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * infrastructure-module의 진입점 설정.
 *
 * <p>이 모듈을 쓰는 앱은 {@code scanBasePackages}에 패키지 문자열을 나열하는 대신
 * 이 클래스를 {@code @Import} 한다. 앱은 자기 패키지만 스캔하고 라이브러리 모듈은 명시적으로
 * 조합하는, Spring Boot 레퍼런스가 제시하는 표준 구성이다.
 *
 * <p>JPA 스캔 단일 소유자인 {@link InfrastructurePersistenceConfig}, 컨텍스트별 {@code <ctx>/config/<Ctx>DomainConfig},
 * {@code QueryDslConfig}는 모두 이 컴포넌트 스캔 범위 안에 있으므로 자동으로 발견된다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan("com.tastyhouse.infrastructure")
public class InfrastructureModuleConfig {
}
