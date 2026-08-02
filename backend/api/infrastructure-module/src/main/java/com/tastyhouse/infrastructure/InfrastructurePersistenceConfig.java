package com.tastyhouse.infrastructure;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * infrastructure-module의 JPA 스캔·전역 설정.
 *
 * <p>이 모듈이 소유한 JPA 엔티티({@code XxxJpaEntity})와 Spring Data 리포지토리를 스캔 대상으로 등록한다.
 * core-module은 이 모듈을 의존하지 않으므로(의존 방향: infrastructure → core) core에
 * 이 패키지를 문자열로 선언할 수 없고, Spring Boot 공식 권장대로 엔티티를 소유한 모듈이 스스로
 * 스캔 설정을 선언한다. {@code basePackageClasses}는 이 클래스가 위치한 패키지
 * ({@code com.tastyhouse.infrastructure}) 이하 전체를 타입 세이프하게 지정한다.
 *
 * <p>core-module이 100% JPA-free로 전환되며(모든 도메인의 {@code @Entity}·{@code JpaRepository}가
 * infrastructure-module로 이동 완료), JPA 감사({@code @EnableJpaAuditing})와 트랜잭션 관리
 * ({@code @EnableTransactionManagement}) 전역 설정도 이 클래스로 병합했다. {@code BaseEntity}의
 * {@code @CreatedDate}/{@code @LastModifiedDate}가 이 설정에 의해 채워진다.
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = InfrastructurePersistenceConfig.class)
@EntityScan(basePackageClasses = InfrastructurePersistenceConfig.class)
@EnableJpaAuditing
@EnableTransactionManagement
public class InfrastructurePersistenceConfig {
}

