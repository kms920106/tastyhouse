package com.tastyhouse.logging.architecture;

import java.nio.file.Path;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

class VisibilityRulesTest {

    @Test
    void topLevelClassesShouldNotBePublic() {
        JavaClasses moduleClasses = new ClassFileImporter()
            .importPath(Path.of("build/classes/java/main"));

        assertThat(moduleClasses).as("모듈 산출물 클래스가 0건이면 규칙이 공허하게 통과한다").isNotEmpty();

        classes()
            .that().areTopLevelClasses()
            .should().notBePublic()
            .because("logging-module은 앱이 runtimeOnly로 싣고 ModuleScanConfig의 문자열 스캔으로만 등록된다 — 어느 모듈도 이 클래스를 import하지 않는다")
            .check(moduleClasses);
    }
}
