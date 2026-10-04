package com.tastyhouse.infrastructure.redis.architecture;

import java.nio.file.Path;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

class PackageRootTest {

    @Test
    void shouldResideInModuleRootPackage() {
        JavaClasses moduleClasses = new ClassFileImporter()
            .importPath(Path.of("build/classes/java/main"));

        assertThat(moduleClasses).as("모듈 산출물 클래스가 0건이면 규칙이 공허하게 통과한다").isNotEmpty();

        classes()
            .should().resideInAPackage("com.tastyhouse.infrastructure.redis..")
            .because("infrastructure 모듈의 루트 패키지는 com.tastyhouse.infrastructure.{모듈명의 하이픈을 점으로} 하나다")
            .check(moduleClasses);
    }
}
