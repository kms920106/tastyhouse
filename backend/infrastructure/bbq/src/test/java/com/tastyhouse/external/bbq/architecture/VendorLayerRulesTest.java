package com.tastyhouse.external.bbq.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class VendorLayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.external.bbq");

    @Test
    void shouldNotDependOnDomain() {
        assertThat(classes).as("벤더 모듈 클래스가 0건이면 규칙이 공허하게 통과한다").isNotEmpty();

        noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.domain..")
            .because("벤더 어댑터는 application의 포트만 구현한다 — 실패는 결과 record로 돌려주고 ErrorCode 번역은 application이 맡는다")
            .check(classes);
    }
}
