package com.tastyhouse.infrastructure.mybatis.architecture;

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
            .should().resideInAPackage("com.tastyhouse.infrastructure.mybatis..")
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
            .because("infrastructure 모듈의 클래스는 앱 ModuleScanConfig의 문자열 스캔으로만 등록되고 다른 패키지가 이름으로 참조하지 않는다")
            .check(moduleClasses);
    }
}
