package com.tastyhouse.apicommon.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
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
        .importPackages("com.tastyhouse.apicommon");

    @Test
    void shouldNotDependOnDomain() {
        assertThat(classes.size())
            .as("모듈이 비면 noClasses() 전역 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(13);

        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.domain..")
            .because("표현 계층은 application만 본다(엄격 레이어드) — 필터·핸들러 단계 에러 계약은 ApiErrorCode, 예외 판정은 application.shared.error.ErrorResponses가 맡는다");

        rule.check(classes);
    }

    @Test
    void scannedComponentsShouldNotBePublic() {
        ArchRule rule = classes()
            .that().areAnnotatedWith("org.springframework.context.annotation.Configuration")
            .or().areAnnotatedWith("org.aspectj.lang.annotation.Aspect")
            .or().areAnnotatedWith("org.springframework.web.bind.annotation.RestControllerAdvice")
            .should().notBePublic()
            .because("rate limit 설정·aspect와 공용 예외 핸들러는 앱 ModuleScanConfig의 문자열 스캔으로만 등록된다 — "
                + "앱이 쓰는 표현 계약(ApiResponse·@RateLimit·ProblemDetails 등)만 public이다");

        rule.check(classes);
    }
}
