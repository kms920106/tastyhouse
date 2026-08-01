package com.tastyhouse.domain.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * domain-module 순수성 규칙(ArchUnit).
 *
 * <p>이 모듈은 <strong>완전 프레임워크-프리</strong>다 — production 의존이 Lombok 하나뿐이며,
 * {@code @Entity}/{@code JpaRepository}/{@code RepositoryImpl}/{@code AttributeConverter}/
 * {@code EntityManager}는 전부 {@code infrastructure-module}에 있다. 도메인 서비스도 전부 순수
 * POJO({@code @Service}/{@code @Transactional} 미사용)이고 빈 등록은 infrastructure-module의
 * {@code DomainServiceConfig}가 {@code @Bean} 팩토리 메서드로 수행한다.
 *
 * <p>이 순수성은 지금까지 {@code build.gradle}의 의존 목록(= 컴파일 실패)으로만 지켜지고 있었는데,
 * 루트 {@code build.gradle}의 {@code subprojects} 블록이 전 모듈에
 * {@code spring-boot-starter}를 주입하고 있어(P10 담당) 실제로는 스프링 타입이 컴파일 클래스패스에
 * 올라온다. 즉 컴파일만으로는 이 원칙이 강제되지 않으므로 ArchUnit 게이트로 명시한다.
 *
 * <p>여기서는 게이트만 세운다 — 루트 빌드의 스프링 주입 제거 자체는 P10 태스크 담당이다.
 *
 * <p>모든 규칙은 {@code allowEmptyShould(true)} 없이 선언한다. 이 모듈은 도메인 클래스가 다수
 * 존재하므로 대상 0건이면 그 자체가 실패로 드러나야 한다(과거 공허 통과가 실제 문제였다).
 */
class DomainPurityTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.domain");

    /**
     * 도메인은 스프링을 알지 않는다. 도메인 서비스는 순수 POJO이고 트랜잭션 경계·빈 등록은
     * 바깥 계층(api 모듈의 CQRS 서비스, infrastructure-module의 {@code DomainServiceConfig})이
     * 담당하므로 {@code @Service}/{@code @Component}/{@code @Transactional}이 등장할 일이 없다.
     */
    @Test
    void domainShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
            .because("domain-module은 프레임워크-프리다(빈 등록·트랜잭션 경계는 바깥 계층 소관)");

        rule.check(classes);
    }

    /**
     * 도메인은 JPA/Jakarta를 알지 않는다. {@code @Entity}·{@code @Embeddable}·{@code @Column} 등
     * 영속화 매핑은 전부 {@code infrastructure-module}의 {@code XxxJpaEntity}가 소유하며, 도메인
     * 모델은 {@code of(...)}/{@code reconstitute(...)} 정적 팩토리만 가진 순수 POJO다.
     * {@code @Embedded} 대상 VO도 어노테이션 없는 {@code record}로 두고 컬럼 매핑은
     * JpaEntity 쪽 {@code @AttributeOverride}가 재선언한다.
     */
    @Test
    void domainShouldNotDependOnJakarta() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage("jakarta..")
            .because("영속화·검증 매핑은 infrastructure-module의 JpaEntity가 소유한다");

        rule.check(classes);
    }

    /**
     * 도메인은 QueryDSL을 알지 않는다. {@code @QueryProjection} Result DTO와 query DAO는 전부
     * {@code infrastructure-module}의 {@code <ctx>/query/} 소유이므로 이 모듈은
     * {@code com.querydsl.*}를 컴파일하지 않는다 — api 모듈로의 QueryDSL 전이 노출을 원천 차단하는
     * 지점이기도 하다.
     */
    @Test
    void domainShouldNotDependOnQuerydsl() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage("com.querydsl..")
            .because("조회 투영은 infrastructure-module의 <ctx>/query/ DAO가 소유한다");

        rule.check(classes);
    }

    /**
     * 의존 방향은 항상 안쪽(domain)을 향한다. 도메인이 자신의 어댑터(infrastructure)나 소비자
     * (web/admin/ceo/batch api 모듈)를 참조하면 헥사고날 의존 역전이 깨진다 — 도메인은 write 포트
     * 인터페이스만 선언하고 그 구현({@code RepositoryImpl})은 바깥에서 주입받는다.
     */
    @Test
    void domainShouldNotDependOnOuterLayers() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.tastyhouse.infrastructure..",
                "com.tastyhouse.webapi..",
                "com.tastyhouse.adminapi..",
                "com.tastyhouse.ceoapi..",
                "com.tastyhouse.batch.."
            )
            .because("의존성은 항상 안쪽(domain)을 향한다 — 도메인은 포트만 선언하고 어댑터를 모른다");

        rule.check(classes);
    }
}
