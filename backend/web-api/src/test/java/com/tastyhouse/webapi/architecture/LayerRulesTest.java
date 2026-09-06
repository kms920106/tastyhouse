package com.tastyhouse.webapi.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.architecture.AppOwnership;
import com.tastyhouse.application.shared.marker.WebApp;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class LayerRulesTest {
    private static final String DOMAIN_ROOT = "com.tastyhouse.domain";

    private static final Set<String> ALLOWED_DOMAIN_ENUM_ACCESSORS =
        Set.of("name", "getDescription", "getDisplayName");

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.webapi");

    @Test
    void controllersShouldNotDependOnRepositories() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository");

        rule.check(classes);
    }

    @Test
    void controllersShouldNotDependOnQueryDaos() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("QueryPort")
            .because("컨트롤러는 조회 어댑터도 읽기 포트도 직접 주입하지 않는다(조회는 QueryService 경유)");

        rule.check(classes);
    }

    @Test
    void shouldNotDependOnQuerydsl() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.querydsl..");

        rule.check(classes);
    }

    @Test
    void shouldNotDependOnInfrastructurePersistence() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..persistence..");

        rule.check(classes);
    }

    @Test
    void controllersShouldBeDomainFree() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat(
                resideInAPackage("com.tastyhouse.domain..")
                    .and(not(resideInAPackage("com.tastyhouse.domain.shared.page..")))
                    .and(not(domainEnum()))
            )
            .because("컨트롤러는 도메인 모델을 import하지 않는다(HTTP 경계는 Long·String). "
                + "페이징 계약과 도메인 enum만 carve-out");

        rule.check(classes);
    }

    @Test
    void shouldDependOnOauthSpiOnlyNotProviderPackages() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.tastyhouse.external.oauth.kakao..",
                "com.tastyhouse.external.oauth.naver..",
                "com.tastyhouse.external.oauth.facebook..",
                "com.tastyhouse.external.oauth.apple.."
            )
            .because("소셜 로그인은 com.tastyhouse.application.auth.port.out을 통해서만 사용한다");

        rule.check(classes);
    }

    @Test
    void requestRecordsShouldBeDomainAndInfraFree() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("..request..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.tastyhouse.domain..",
                "com.tastyhouse.infrastructure.."
            )
            .because("Request record는 domain-free·infra-free 순수 데이터 홀더다");

        rule.check(classes);
    }

    @Test
    void controllersShouldDependOnUseCasesOnly() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("CommandService")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("QueryService")
            .because("컨트롤러는 UseCase 인터페이스만 주입한다(구체 서비스 금지)");

        rule.check(classes);
    }

    @Test
    void webAdaptersShouldNotDependOnApplicationServices() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..adapter.in.web..")
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.application..service..")
            .because("인바운드 어댑터는 UseCase 인터페이스만 주입한다(구체 서비스 금지)");

        rule.check(classes);
    }

    @Test
    void apiModuleMustNotContainApplicationLayer() {
        ArchRule rule = noClasses()
            .should().beAnnotatedWith("org.springframework.stereotype.Service")
            .because("application 계층은 web-application 모듈이 소유한다(web-api에 @Service 금지)");

        rule.check(classes);
    }

    @Test
    void restControllersShouldResideInWebAdapterPackage() {
        ArchRule rule = classes()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .should().resideInAPackage("..adapter.in.web..")
            .because("컨트롤러는 인바운드 어댑터 패키지에만 둔다(3층 구조)");

        rule.check(classes);
    }

    @Test
    void apiModuleShouldBeDomainModelFree() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat(
                resideInAPackage("com.tastyhouse.domain..")
                    .and(not(resideInAPackage("com.tastyhouse.domain.exception..")))
                    .and(not(resideInAPackage("com.tastyhouse.domain.shared.page..")))
                    .and(not(domainEnum()))
                    .as("도메인 모델(enum·에러 계약·페이징 계약 제외)")
            )
            .because("api 모듈은 도메인 모델을 알지 않는다(승격은 application 서비스 담당). "
                + "enum은 짝 규칙이 읽기 accessor만 허용하는 조건으로 carve-out");

        rule.check(classes);
    }

    @Test
    void apiModuleShouldOnlyReadDomainEnums() {
        ArchRule rule = noClasses()
            .should().callMethodWhere(DescribedPredicate.describe(
                "도메인 enum의 비-accessor 호출",
                (JavaMethodCall call) -> domainEnum().test(call.getTargetOwner())
                    && !ALLOWED_DOMAIN_ENUM_ACCESSORS.contains(call.getName())))
            .because("api 모듈은 도메인 enum의 읽기 전용 accessor만 호출한다"
                + "(from(String) 승격·상태 전이 판정·등급 계산은 application·domain 담당)");

        rule.check(classes);
    }

    @Test
    void domainBoundaryPredicatesShouldStillBite() {
        JavaClasses domainClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(DOMAIN_ROOT);

        DescribedPredicate<JavaClass> forbidden = resideInAPackage("com.tastyhouse.domain..")
            .and(not(resideInAPackage("com.tastyhouse.domain.exception..")))
            .and(not(resideInAPackage("com.tastyhouse.domain.shared.page..")))
            .and(not(domainEnum()));

        JavaClass shop = domainClasses.get("com.tastyhouse.domain.shop.model.Shop");
        JavaClass memberGrade = domainClasses.get("com.tastyhouse.domain.member.model.MemberGrade");

        if (!forbidden.test(shop)) {
            throw new AssertionError(
                "carve-out이 너무 넓습니다 — 애그리거트 루트 Shop이 허용 대상이 됐습니다. "
                    + "enum 타입 성격 술어를 패키지 술어로 되돌리지 않았는지 확인하세요.");
        }

        if (!memberGrade.isEnum() || forbidden.test(memberGrade)) {
            throw new AssertionError("도메인 enum MemberGrade가 carve-out되지 않았습니다.");
        }

        boolean hasLogicMethodOutsideAllowList = memberGrade.getMethods().stream()
            .anyMatch(method -> method.getName().equals("fromReviewCount")
                && !ALLOWED_DOMAIN_ENUM_ACCESSORS.contains(method.getName()));
        if (!hasLogicMethodOutsideAllowList) {
            throw new AssertionError(
                "전제가 바뀌었습니다 — MemberGrade#fromReviewCount가 없거나 허용 목록에 들어갔습니다. "
                    + "짝 규칙 apiModuleShouldOnlyReadDomainEnums가 무엇을 막는지 재검토하세요.");
        }

        long enumsInAggregatePackages = domainClasses.stream()
            .filter(JavaClass::isEnum)
            .filter(javaClass -> javaClass.getPackageName().startsWith(DOMAIN_ROOT))
            .filter(javaClass -> javaClass.getPackageName().endsWith(".model"))
            .count();
        if (enumsInAggregatePackages == 0) {
            throw new AssertionError(
                "전제가 바뀌었습니다 — '..model' 패키지에 도메인 enum이 더는 없습니다. "
                    + "enum이 자기 패키지로 분리됐다면 타입 성격 술어를 패키지 술어로 단순화할 수 "
                    + "있는지 재검토하세요.");
        }
        if (!shop.getPackageName().endsWith(".model")) {
            throw new AssertionError(
                "전제가 바뀌었습니다 — 애그리거트 루트 Shop이 '..model' 패키지에 없습니다: "
                    + shop.getPackageName());
        }
    }

    private static DescribedPredicate<JavaClass> domainEnum() {
        return DescribedPredicate.describe("도메인 enum",
            javaClass -> javaClass.isEnum()
                && javaClass.getPackageName().startsWith(DOMAIN_ROOT)
                && !javaClass.getPackageName().startsWith(DOMAIN_ROOT + ".exception"));
    }

    @Test
    void adaptersShouldOnlyUseOwnAppUseCases() {
        JavaClasses applicationClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.tastyhouse.application");

        Map<String, Set<Class<? extends Annotation>>> commandApps = new HashMap<>();
        AppOwnership.derive(applicationClasses)
            .forEach((record, apps) -> commandApps.put(record.getName(), apps));

        List<String> violations = new ArrayList<>();
        for (JavaClass adapter : classes) {
            for (JavaClass dependency : adapter.getDirectDependenciesFromSelf().stream()
                .map(Dependency::getTargetClass).toList()) {
                if (!dependency.getPackageName().contains(".port.in")) {
                    continue;
                }
                if (dependency.isInterface()) {
                    if (!dependency.isAnnotatedWith(WebApp.class)) {
                        violations.add(adapter.getName() + " -> " + dependency.getName()
                            + " (다른 앱의 UseCase — @WebApp가 아니다)");
                    }
                } else if (dependency.isRecord()) {
                    Set<Class<? extends Annotation>> apps = commandApps.get(dependency.getName());
                    if (apps != null && !apps.equals(Set.of(WebApp.class))) {
                        violations.add(adapter.getName() + " -> " + dependency.getName()
                            + " (소속 앱 " + AppOwnership.describe(apps) + " — @WebApp가 아니다)");
                    }
                }
            }
        }

        assertThat(violations)
            .as("인바운드 어댑터는 자기 앱(@WebApp)의 application 슬라이스만 의존한다")
            .isEmpty();
    }
}
