package com.tastyhouse.infrastructure.architecture;

import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class LayerRulesTest {
    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.infrastructure");

    @Test
    void shouldNotDependOnApiModules() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat(
                resideInAnyPackage(
                    "com.tastyhouse.webapi..",
                    "com.tastyhouse.adminapi..",
                    "com.tastyhouse.ceoapi..",
                    "com.tastyhouse.batch..",
                    "com.tastyhouse.application.."
                ).and(not(resideInAPackage("com.tastyhouse.application..port.out.."))))
            .because("의존 방향은 api → infrastructure → domain 한 방향이다 — "
                + "infra는 application의 아웃바운드 포트(port.out)를 구현하고 유스케이스는 침범하지 않는다");

        rule.check(classes);
    }

    private static final Set<String> SEALED_PERSISTENCE_TO_QUERY = Set.of(
        "com.tastyhouse.infrastructure.product.persistence.ProductReviewStatisticsAdapter",
        "com.tastyhouse.infrastructure.rank.persistence.MemberReviewCountAdapter",
        "com.tastyhouse.infrastructure.search.persistence.KeywordCountAdapter"
    );

    @Test
    void persistenceShouldNotDependOnQuery() {
        ArchRule rule = noClasses()
            .that(resideInAPackage("com.tastyhouse.infrastructure..persistence..")
                .and(not(sealed()))
                .as("..persistence.. (봉인 제외)"))
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure..query..")
            .because("write 어댑터는 read model을 의존하지 않는다(read→write 단방향)");

        rule.check(classes);
    }

    @Test
    void sealedPersistenceToQueryShouldNotBeStale() {
        for (String sealedName : SEALED_PERSISTENCE_TO_QUERY) {
            ArchRule stillViolates = noClasses()
                .that(hasName(sealedName))
                .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure..query..");

            if (!stillViolates.evaluate(classes).hasViolation()) {
                throw new AssertionError(
                    "봉인 목록이 낡았습니다 — 더 이상 위반하지 않으므로 SEALED_PERSISTENCE_TO_QUERY에서 제거하세요: " + sealedName);
            }
        }
    }

    @Test
    void sealedPersistenceToQueryListShouldNotBeEmpty() {
        if (SEALED_PERSISTENCE_TO_QUERY.isEmpty()) {
            throw new AssertionError(
                "봉인 목록이 비었습니다 — SEALED_PERSISTENCE_TO_QUERY와 짝 테스트를 제거하고 순수 강제로 전환하세요.");
        }
    }

    private static final Set<String> INFRA_OWNED_QUERY_PORTS = Set.of(
        "com.tastyhouse.infrastructure.review.query.MemberReviewCountQueryPort"
    );

    @Test
    void queryDaosShouldImplementQueryPorts() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("QueryDao")
            .should().implement(
                resideInAPackage("com.tastyhouse.application..port.out..")
                    .or(infraOwnedQueryPort()))
            .because("조회 계약은 응용 계층이 소유하고 DAO가 구현한다. "
                + "단 application 소비자가 없는 내부 투영 계약은 infra가 자체 소유한다(봉인 목록)");

        rule.check(classes);
    }

    @Test
    void infraOwnedQueryPortListShouldNotBeStale() {
        for (String portName : INFRA_OWNED_QUERY_PORTS) {
            if (!classes.contain(portName)) {
                throw new AssertionError(
                    "봉인 목록이 낡았습니다 — infra에 더 이상 존재하지 않으므로 INFRA_OWNED_QUERY_PORTS에서 제거하세요: "
                        + portName);
            }
        }
    }

    @Test
    void infraOwnedQueryPortListShouldNotBeEmpty() {
        if (INFRA_OWNED_QUERY_PORTS.isEmpty()) {
            throw new AssertionError(
                "봉인 목록이 비었습니다 — INFRA_OWNED_QUERY_PORTS와 짝 테스트를 제거하고 "
                    + "queryDaosShouldImplementQueryPorts를 순수 강제로 되돌리세요.");
        }
    }

    private static DescribedPredicate<JavaClass> infraOwnedQueryPort() {
        return DescribedPredicate.describe(
            "infra 자체 소유 읽기 계약",
            javaClass -> INFRA_OWNED_QUERY_PORTS.contains(javaClass.getName()));
    }

    private static DescribedPredicate<JavaClass> sealed() {
        return DescribedPredicate.describe(
            "봉인된 포트 어댑터",
            javaClass -> SEALED_PERSISTENCE_TO_QUERY.contains(javaClass.getName()));
    }

    private static DescribedPredicate<JavaClass> hasName(String fullyQualifiedName) {
        return DescribedPredicate.describe(
            fullyQualifiedName,
            javaClass -> javaClass.getName().equals(fullyQualifiedName));
    }
}
