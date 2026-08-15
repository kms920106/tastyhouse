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
 * 컨텍스트별 {@code <Ctx>DomainConfig}가 {@code @Bean} 팩토리 메서드로 수행한다.
 *
 * <p>이 순수성은 두 겹으로 강제된다 — 루트 {@code build.gradle}이 domain-module을 spring 주입
 * {@code subprojects} 블록에서 제외해 컴파일 클래스패스에 {@code org.springframework.*}가 아예
 * 없고(컴파일 게이트), 그 위에 이 ArchUnit 규칙이 선다. 컴파일 게이트는 "클래스패스에 없어서 못 쓴다"를,
 * ArchUnit은 "클래스패스에 있어도(예: 테스트 의존으로 들어온 타입) 쓰면 안 된다"를 각각 막는다.
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
     * 바깥 계층(api 모듈의 CQRS 서비스, infrastructure-module의 컨텍스트별 {@code <Ctx>DomainConfig})이
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
