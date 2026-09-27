package com.tastyhouse.adminapi.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.architecture.AppOwnership;
import com.tastyhouse.application.shared.marker.AdminApp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class LayerRulesTest {
    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.adminapi");

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
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.domain..")
            .because("컨트롤러는 도메인 모델을 import하지 않는다(HTTP 경계는 Long·String, 조회 결과는 application Result)");

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
            .because("application 계층은 admin-application 모듈이 소유한다(admin-api에 @Service 금지)");

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
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.domain..")
            .because("api 모듈은 application만 본다(엄격 레이어드). 예외 판정은 ErrorResponses, "
                + "페이징은 application 페이징 계약, enum은 Result의 String으로 받는다");

        rule.check(classes);
    }

    @Test
    void seedersShouldDependOnUseCasesOnly() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..config..")
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.application..service..")
            .because("부트스트랩 시더도 UseCase 인터페이스만 주입한다(구체 서비스 금지)");

        rule.check(classes);
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
                    if (!dependency.isAnnotatedWith(AdminApp.class)) {
                        violations.add(adapter.getName() + " -> " + dependency.getName()
                            + " (다른 앱의 UseCase — @AdminApp가 아니다)");
                    }
                } else if (dependency.isRecord()) {
                    Set<Class<? extends Annotation>> apps = commandApps.get(dependency.getName());
                    if (apps != null && !apps.equals(Set.of(AdminApp.class))) {
                        violations.add(adapter.getName() + " -> " + dependency.getName()
                            + " (소속 앱 " + AppOwnership.describe(apps) + " — @AdminApp가 아니다)");
                    }
                }
            }
        }

        assertThat(violations)
            .as("인바운드 어댑터는 자기 앱(@AdminApp)의 application 슬라이스만 의존한다")
            .isEmpty();
    }
}
