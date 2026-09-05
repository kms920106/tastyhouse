package com.tastyhouse.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.security.jwt.JwtProperties;

/**
 * security-module의 auto-configuration — JWT 인증, Redis 토큰 저장소.
 *
 * <p>클래스패스 존재만으로 활성화되므로, 이 모듈에 의존하는 앱(web-api·admin-api·ceo-api)에서만
 * 발화한다. batch-module은 이 모듈을 의존하지 않아 jar 자체가 클래스패스에 없다.
 * 다만 전이로 끌려오더라도 서블릿 웹 앱이 아니면 발화하지 않도록 조건을 명시한다 —
 * 이 모듈의 빈은 서블릿 필터 체인 전제이기 때문이다.
 *
 * <p>{@code @ConfigurationProperties} record는 컴포넌트 스캔 대신 여기서 명시적으로 등록한다.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ComponentScan("com.tastyhouse.security")
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityModuleAutoConfiguration {
}
