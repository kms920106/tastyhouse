package com.tastyhouse.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.security.jwt.JwtProperties;

/**
 * security-module의 진입점 설정 — JWT 인증, 레이트 리밋, Redis 토큰 저장소.
 *
 * <p>인증이 필요한 앱(web-api·admin-api·ceo-api)만 {@code @Import} 한다. batch-module은
 * 인증 빈이 필요 없으므로 import 하지 않으며, 따라서 {@link JwtProperties}도 등록되지 않는다.
 *
 * <p>{@code @ConfigurationProperties} record는 컴포넌트 스캔 대신 여기서 명시적으로 등록한다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan("com.tastyhouse.security")
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityModuleConfig {
}
