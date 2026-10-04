package com.tastyhouse.infrastructure.persistence.architecture;

import java.nio.file.Path;
import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.infrastructure.persistence");

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

    @Test
    void queryShouldNotDependOnDomain() {
        ArchRule rule = noClasses()
            .that(resideInAPackage("com.tastyhouse.infrastructure.persistence..query..")
                .or(sealed())
                .as("..query.. 와 봉인된 조회 어댑터"))
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.domain..")
            .because("조회 어댑터는 domain-free 읽기 계약만 구현한다 — 도메인 모델을 쓰는 것은 영속 어댑터(XxxPersistenceAdapter)뿐이다");

        rule.check(classes);
    }

    private static final Set<String> SEALED_PERSISTENCE_TO_QUERY = Set.of(
        "com.tastyhouse.infrastructure.persistence.product.persistence.ProductReviewStatisticsAdapter",
        "com.tastyhouse.infrastructure.persistence.rank.persistence.MemberReviewCountAdapter",
        "com.tastyhouse.infrastructure.persistence.search.persistence.KeywordCountAdapter"
    );

    @Test
    void persistenceShouldNotDependOnQuery() {
        ArchRule rule = noClasses()
            .that(resideInAPackage("com.tastyhouse.infrastructure.persistence..persistence..")
                .and(not(sealed()))
                .as("..persistence.. (봉인 제외)"))
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure.persistence..query..")
            .because("write 어댑터는 read model을 의존하지 않는다(read→write 단방향)");

        rule.check(classes);
    }

    @Test
    void sealedPersistenceToQueryShouldNotBeStale() {
        for (String sealedName : SEALED_PERSISTENCE_TO_QUERY) {
            ArchRule stillViolates = noClasses()
                .that(hasName(sealedName))
                .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure.persistence..query..");

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
        "com.tastyhouse.infrastructure.persistence.review.query.MemberReviewCountQueryPort"
    );

    @Test
    void queryAdaptersShouldImplementQueryPorts() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("QueryAdapter")
            .should().implement(
                resideInAPackage("com.tastyhouse.application..port.out..")
                    .or(infraOwnedQueryPort()))
            .because("조회 계약은 응용 계층이 소유하고 조회 어댑터가 구현한다. "
                + "단 application 소비자가 없는 내부 투영 계약은 infra가 자체 소유한다(봉인 목록)");

        rule.check(classes);
    }

    @Test
    void adaptersShouldNotUseRetiredSuffixes() {
        ArchRule rule = noClasses()
            .should().haveSimpleNameEndingWith("RepositoryImpl")
            .orShould().haveSimpleNameEndingWith("QueryDao")
            .because("영속 어댑터는 XxxPersistenceAdapter, 조회 어댑터는 XxxQueryAdapter로 짓는다 — "
                + "옛 접미어를 쓰면 queryAdaptersShouldImplementQueryPorts 등 이름 기반 규칙의 대상에서 빠진다");

        rule.check(classes);
    }

    @Test
    void queryAdaptersShouldNotUseTuple() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().haveFullyQualifiedName("com.querydsl.core.Tuple")
            .orShould().dependOnClassesThat().haveFullyQualifiedName("com.querydsl.core.types.QTuple")
            .orShould().dependOnClassesThat().haveFullyQualifiedName("com.querydsl.core.types.MappingProjection")
            .because("Tuple은 타입 없는 행이라 위치 접근·같은 타입 컬럼 순서 착오가 컴파일을 통과한다 — "
                + "다중 컬럼 select는 Projections.constructor로 public XxxRow record에 투영한다");

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
                    + "queryAdaptersShouldImplementQueryPorts를 순수 강제로 되돌리세요.");
        }
    }

    @Test
    void shouldResideInModuleRootPackage() {
        JavaClasses moduleClasses = new ClassFileImporter()
            .importPath(Path.of("build/classes/java/main"));

        assertThat(moduleClasses).as("모듈 산출물 클래스가 0건이면 규칙이 공허하게 통과한다").isNotEmpty();

        classes()
            .should().resideInAPackage("com.tastyhouse.infrastructure.persistence..")
            .because("infrastructure 모듈의 루트 패키지는 com.tastyhouse.infrastructure.{모듈명의 하이픈을 점으로} 하나다")
            .check(moduleClasses);
    }

    private static final Set<String> PUBLIC_BY_NECESSITY = Set.of(
        "com.tastyhouse.infrastructure.persistence.file.query.FileUrlResolver",
        "com.tastyhouse.infrastructure.persistence.menureview.query.MenuReviewStatisticsQueryAdapter",
        "com.tastyhouse.infrastructure.persistence.review.query.MemberReviewCountQueryAdapter",
        "com.tastyhouse.infrastructure.persistence.search.query.SearchQueryAdapter",
        "com.tastyhouse.infrastructure.persistence.shop.persistence.ShopJpaEntity"
    );

    @Test
    void topLevelClassesShouldNotBePublic() {
        ArchRule rule = classes()
            .that().areTopLevelClasses()
            .and(not(publicByCategory()))
            .and(not(publicByNecessity()))
            .should().notBePublic()
            .because("persistence의 엔티티·JPA 리포지토리·어댑터는 앱 ModuleScanConfig의 문자열 스캔과 "
                + "@EnableJpaRepositories로만 등록된다 — public이 없어야 다른 패키지가 구현에 직접 결합하는 것을 컴파일러가 막는다");

        rule.check(classes);
    }

    @Test
    void publicByNecessityShouldStillBePublic() {
        for (String name : PUBLIC_BY_NECESSITY) {
            if (!classes.get(name).getModifiers().contains(JavaModifier.PUBLIC)) {
                throw new AssertionError(
                    "허용 목록이 낡았습니다 — 더 이상 public이 아니므로 PUBLIC_BY_NECESSITY에서 제거하세요: " + name);
            }
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

    private static DescribedPredicate<JavaClass> publicByCategory() {
        return DescribedPredicate.describe(
            "범주상 public이어야 하는 타입(조회 투영 record·infra 소유 읽기 계약·BaseEntity·Embeddable·QueryDSL Q타입)",
            javaClass -> (javaClass.isAssignableTo(Record.class) && javaClass.getPackageName().endsWith(".query"))
                || javaClass.getSimpleName().endsWith("QueryPort")
                || javaClass.getSimpleName().equals("BaseEntity")
                || javaClass.getSimpleName().endsWith("Embeddable")
                || javaClass.isAssignableTo("com.querydsl.core.types.dsl.BeanPath"));
    }

    private static DescribedPredicate<JavaClass> publicByNecessity() {
        return DescribedPredicate.describe(
            "다른 패키지가 참조해 public이어야 하는 타입(허용 목록)",
            javaClass -> PUBLIC_BY_NECESSITY.contains(javaClass.getName()));
    }
}
