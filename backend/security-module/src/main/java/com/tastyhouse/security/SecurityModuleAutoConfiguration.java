package com.tastyhouse.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.security.jwt.JwtAuthenticationFilter;
import com.tastyhouse.security.jwt.JwtProperties;
import com.tastyhouse.security.jwt.JwtTokenProvider;
import com.tastyhouse.security.token.BlacklistRepository;

/**
 * security-module의 auto-configuration — JWT 인증, Redis 토큰 저장소.
 *
 * <p>클래스패스 존재만으로 활성화되므로, 이 모듈에 의존하는 앱(web-api·admin-api·ceo-api)에서만
 * 발화한다. batch-module은 이 모듈을 의존하지 않아 jar 자체가 클래스패스에 없다.
 * 다만 전이로 끌려오더라도 서블릿 웹 앱이 아니면 발화하지 않도록 조건을 명시한다 —
 * 이 모듈의 빈은 서블릿 필터 체인 전제이기 때문이다.
 *
 * <p>{@code @ConfigurationProperties} record는 컴포넌트 스캔 대신 여기서 명시적으로 등록한다.
 *
 * <p>{@link JwtAuthenticationFilter}는 POJO라 스캔 대상이 아니므로 여기서 빈 메서드로 등록한다.
 * 앱 컨텍스트마다 {@link JwtTokenProvider} 타입 빈이 정확히 하나(앱 마커로 걸러진 하위 클래스)여서
 * 타입 주입이 모호하지 않다.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ComponentScan("com.tastyhouse.security")
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityModuleAutoConfiguration {

    /**
     * 앱의 {@link JwtTokenProvider} 하위 빈과 {@link BlacklistRepository} 포트로 인증 필터를 등록한다.
     *
     * <p>{@link ConditionalOnMissingBean}은 어떤 앱이 자기 필터를 등록해 덮어야 할 때의 escape hatch다
     * (현재 그런 앱은 없다). 앱의 {@code @Configuration}이 먼저 파싱되므로 앱이 자기 필터 빈을 정의하면
     * 이 기본 등록은 물러난다.
     *
     * <p><b>단, provider 빈이 둘 이상 생기는 경우의 해법은 {@code @Primary}(또는 한정자)뿐이다</b> —
     * 이 조건은 {@code JwtAuthenticationFilter} <b>타입</b>만 보므로 provider가 모호해도 조건은 그대로
     * 통과하고, 빈 메서드를 실제로 호출하는 시점에 {@code NoUniqueBeanDefinitionException}으로 기동이
     * 실패한다. 즉 필터 재정의는 provider 모호성을 막아주지 못한다.
     */
    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationFilter.class)
    public JwtAuthenticationFilter jwtAuthenticationFilter(
        JwtTokenProvider jwtTokenProvider,
        BlacklistRepository blacklistRepository,
        ObjectMapper objectMapper
    ) {
        return new JwtAuthenticationFilter(jwtTokenProvider, blacklistRepository, objectMapper);
    }
}
