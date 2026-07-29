package com.tastyhouse.batch.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * batch-module 레이어 경계 규칙(ArchUnit).
 *
 * <p>core-module → domain-module 전환 목표 구조를 컴파일 게이트로 강제한다. batch는 HTTP
 * 컨트롤러가 없어 web/admin/ceo의 {@code controllersShouldNotDependOnRepositories} 규칙은 두지 않고,
 * api 모듈 공통의 QueryDSL·persistence 차단 규칙을 적용한다. {@code ..application..} 패키지는 전환이
 * 진행되며 채워지므로, 아직 클래스가 없을 때도 규칙이 공허하게 통과하도록
 * {@code allowEmptyShould(true)}를 둔다.
 */
class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.batch");

    @Test
    void applicationShouldNotDependOnWebLayer() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework.web..",
                "jakarta.servlet..",
                "..request..",
                "..response.."
            )
            .allowEmptyShould(true);

        rule.check(classes);
    }

    /**
     * api 모듈은 QueryDSL을 알지 않는다. 조회는 infrastructure-module의 {@code <ctx>/query/} DAO가
     * 캡슐화하며, 이 모듈은 그 DAO와 Result DTO만 주입·import한다.
     */
    @Test
    void shouldNotDependOnQuerydsl() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.querydsl..")
            .allowEmptyShould(true);

        rule.check(classes);
    }

    /**
     * api 모듈은 infra 중 {@code ..query..}(직접)·{@code ..listener..}(간접)만 허용하며,
     * persistence 어댑터(JpaEntity/Mapper/JpaRepository/RepositoryImpl)에 직접 의존하지 않는다.
     */
    @Test
    void shouldNotDependOnInfrastructurePersistence() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..persistence..")
            .allowEmptyShould(true);

        rule.check(classes);
    }
}
