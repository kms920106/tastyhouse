package com.tastyhouse.infrastructure;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * infrastructure-module의 JPA 스캔 설정.
 *
 * <p>이 모듈이 소유한 JPA 엔티티({@code XxxJpaEntity})와 Spring Data 리포지토리를 스캔 대상으로 등록한다.
 * core-module은 이 모듈을 의존하지 않으므로(의존 방향: infrastructure → core) core의
 * {@code DatabaseConfig}에 이 패키지를 문자열로 선언할 수 없고, Spring Boot 공식 권장대로
 * 엔티티를 소유한 모듈이 스스로 스캔 설정을 선언한다. {@code basePackageClasses}는 이 클래스가
 * 위치한 패키지({@code com.tastyhouse.infrastructure}) 이하 전체를 타입 세이프하게 지정한다.
 * {@code @EntityScan}은 여러 설정에 선언되면 패키지가 누적 병합되므로 core의 설정과 공존한다.
 * JPA Auditing 등 전역 설정은 core의 {@code DatabaseConfig}가 담당하며 여기서 중복 선언하지 않는다.
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = InfrastructurePersistenceConfig.class)
@EntityScan(basePackageClasses = InfrastructurePersistenceConfig.class)
public class InfrastructurePersistenceConfig {
}
