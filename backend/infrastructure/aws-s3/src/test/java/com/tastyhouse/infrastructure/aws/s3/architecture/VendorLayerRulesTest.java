package com.tastyhouse.infrastructure.aws.s3.architecture;

import java.nio.file.Path;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class VendorLayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.infrastructure.aws.s3");

    @Test
    void shouldNotDependOnDomain() {
        assertThat(classes).as("벤더 모듈 클래스가 0건이면 규칙이 공허하게 통과한다").isNotEmpty();

        noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.domain..")
            .because("벤더 어댑터는 application의 포트만 구현한다 — 실패는 결과 record로 돌려주고 ErrorCode 번역은 application이 맡는다")
            .check(classes);
    }

    @Test
    void shouldResideInModuleRootPackage() {
        JavaClasses moduleClasses = new ClassFileImporter()
            .importPath(Path.of("build/classes/java/main"));

        assertThat(moduleClasses).as("모듈 산출물 클래스가 0건이면 규칙이 공허하게 통과한다").isNotEmpty();

        classes()
            .should().resideInAPackage("com.tastyhouse.infrastructure.aws.s3..")
            .because("infrastructure 모듈의 루트 패키지는 com.tastyhouse.infrastructure.{모듈명의 하이픈을 점으로} 하나다")
            .check(moduleClasses);
    }

    @Test
    void topLevelClassesShouldNotBePublic() {
        JavaClasses moduleClasses = new ClassFileImporter()
            .importPath(Path.of("build/classes/java/main"));

        assertThat(moduleClasses).as("모듈 산출물 클래스가 0건이면 규칙이 공허하게 통과한다").isNotEmpty();

        classes()
            .that().areTopLevelClasses()
            .should().notBePublic()
            .because("벤더 모듈의 클래스는 앱 ModuleScanConfig의 문자열 스캔으로만 등록되고 어느 모듈도 import하지 않는다 — public이 없어야 다른 모듈이 벤더 구현에 직접 결합하는 것을 컴파일러가 막는다")
            .check(moduleClasses);
    }
}
