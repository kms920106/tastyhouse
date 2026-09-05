package com.tastyhouse.infrastructure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * infrastructure:persistence 모듈의 auto-configuration.
 *
 * <p>이 모듈이 앱의 runtimeClasspath에 있으면 자동으로 활성화된다("클래스패스 존재 = 활성화").
 * 앱은 이 클래스를 {@code @Import} 하지 않으며, {@code build.gradle}에서 {@code runtimeOnly}로만
 * 의존한다. 끄려면 {@code spring.autoconfigure.exclude}를 쓴다.
 *
 * <p>JPA 스캔 단일 소유자인 {@link InfrastructurePersistenceConfig}, 컨텍스트별
 * {@code <ctx>/config/<Ctx>DomainConfig}, {@code QueryDslConfig}는 모두 이 컴포넌트 스캔 범위
 * 안에 있으므로 자동으로 발견된다.
 *
 * <p><b>{@code infrastructure.redis} 제외</b> — Redis 빈의 등록 주체는
 * {@code RedisModuleAutoConfiguration} 하나로 일원화한다. 제외하지 않으면 redis 모듈이
 * 클래스패스에 있을 때 두 스캔이 같은 클래스를 중복 등록한다.
 *
 * <p><b>{@code before = JpaRepositoriesAutoConfiguration}</b> — 스캔 안
 * {@link InfrastructurePersistenceConfig}의 {@code @EnableJpaRepositories}가 같은 deferred 단계에서
 * Boot보다 먼저 처리돼야, Boot 쪽 {@code @ConditionalOnMissingBean(JpaRepositoryConfigExtension)}이
 * 물러난다.
 */
@AutoConfiguration(before = JpaRepositoriesAutoConfiguration.class)
@ComponentScan(
    basePackages = "com.tastyhouse.infrastructure",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.tastyhouse\\.infrastructure\\.redis\\..*"
    )
)
public class PersistenceModuleAutoConfiguration {
}
