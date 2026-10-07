package com.tastyhouse.webapi.architecture;

import java.util.List;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.webapi");

    @Test
    void controllersShouldNotDependOnPersistencePorts() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("PersistencePort")
            .orShould().dependOnClassesThat().resideInAPackage("com.tastyhouse.application..port.out.write..")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
            .because("컨트롤러는 쓰기 포트(port.out.write)도 Spring Data·토큰 저장소(XxxJpaRepository·XxxTokenRepository)도 직접 주입하지 않는다");

        rule.check(classes);
    }

    @Test
    void controllersShouldNotDependOnQueryPorts() {
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
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure.persistence..");

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
    void shouldDependOnOauthSpiOnlyNotProviderPackages() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.tastyhouse.infrastructure.kakao.oauth..",
                "com.tastyhouse.infrastructure.naver.oauth..",
                "com.tastyhouse.infrastructure.facebook.oauth..",
                "com.tastyhouse.infrastructure.apple.oauth.."
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
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.domain..")
            .because("api 모듈은 application만 본다(엄격 레이어드). 예외 판정은 ErrorResponses, "
                + "페이징은 application 페이징 계약, enum은 Result의 String으로 받는다");

        rule.check(classes);
    }

    @Test
    void useCaseFieldsShouldBeNamedUseCase() {
        List<JavaField> useCaseFields = classes.stream()
            .flatMap(javaClass -> javaClass.getFields().stream())
            .filter(field -> isPortInInterface(field.getRawType()))
            .toList();

        assertThat(useCaseFields)
            .as("UseCase 타입 필드가 줄면 타입 해석이 깨져 이 규칙이 공허하게 통과할 수 있다")
            .hasSizeGreaterThanOrEqualTo(40);

        List<String> violations = useCaseFields.stream()
            .filter(field -> !field.getName().endsWith("UseCase"))
            .map(JavaField::getFullName)
            .toList();

        assertThat(violations)
            .as("port.in 인터페이스 타입 필드는 이름이 UseCase로 끝난다(변수명은 타입을 따른다)")
            .isEmpty();
    }

    private static boolean isPortInInterface(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        return javaClass.isInterface()
            && (packageName.endsWith(".port.in") || packageName.contains(".port.in."));
    }

    @Test
    void controllersAndConfigsShouldNotBePublic() {
        ArchRule rule = classes()
            .that(DescribedPredicate.describe(
                "@RestController·@RestControllerAdvice 또는 config 패키지의 클래스(*SeedProperties 제외)",
                javaClass -> javaClass.isAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    || javaClass.isAnnotatedWith("org.springframework.web.bind.annotation.RestControllerAdvice")
                    || (isConfigPackage(javaClass.getPackageName()) && !javaClass.getSimpleName().endsWith("SeedProperties"))))
            .and().areTopLevelClasses()
            .should().notBePublic()
            .because("컨트롤러·예외 핸들러·설정은 컴포넌트 스캔으로만 등록되고 어떤 클래스도 직접 참조하지 않는다 — "
                + "*SeedProperties는 부트스트랩이 @EnableConfigurationProperties로 참조하므로 public이다");

        rule.check(classes);
    }

    private static boolean isConfigPackage(String packageName) {
        return packageName.endsWith(".config") || packageName.contains(".config.");
    }
}
