package com.tastyhouse.apicommon.ratelimit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * api-common-module의 rate limit auto-configuration — {@link RateLimitAspect} 등록.
 *
 * <p>카운터 구현체({@code RedisRateLimitCounter})는 {@code infrastructure:redis} 모듈이
 * 등록한다. 이 설정은 그 구현이 실제로 있을 때만 aspect를 올린다.
 *
 * <p><b>{@code afterName}에 클래스 리터럴을 쓸 수 없는 이유</b> — 의존 방향이
 * {@code infrastructure:redis → api-common-module}이라 api-common은 redis 모듈의 타입을
 * 컴파일 시점에 볼 수 없다(순환). 문자열 FQCN으로 순서만 선언한다. 스캔된
 * {@code RedisRateLimitCounter} 정의는 redis auto-config 처리 시점에 등록되므로,
 * 이 순서가 {@code @ConditionalOnBean}의 가시성을 보장한다.
 *
 * <p><b>프로퍼티 스위치를 두지 않는다.</b> web-api뿐 아니라 admin-api·ceo-api의 로그인
 * 엔드포인트도 {@code @RateLimit(IP, 10회/60초)}로 이 aspect에 의존한다. 앱별 on/off 프로퍼티는
 * 그 보호를 조용히 제거하는 보안 회귀가 되므로, 조건은 "카운터 빈이 있는 서블릿 앱"뿐이다.
 */
@AutoConfiguration(afterName = "com.tastyhouse.infrastructure.redis.RedisModuleAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ApiCommonRateLimitAutoConfiguration {

    @Bean
    @ConditionalOnBean(RateLimitCounterPort.class)
    public RateLimitAspect rateLimitAspect(RateLimitCounterPort rateLimitCounter) {
        return new RateLimitAspect(rateLimitCounter);
    }
}
