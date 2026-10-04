package com.tastyhouse.infrastructure.redis.architecture;

import java.nio.file.Path;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

class PackageRootTest {

    private static final String PUBLIC_BY_NECESSITY = "com.tastyhouse.infrastructure.redis.token.RedisTokenStoreProperties";

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

    @Test
    void topLevelClassesShouldNotBePublic() {
        JavaClasses moduleClasses = new ClassFileImporter()
            .importPath(Path.of("build/classes/java/main"));

        assertThat(moduleClasses).as("모듈 산출물 클래스가 0건이면 규칙이 공허하게 통과한다").isNotEmpty();

        classes()
            .that().areTopLevelClasses()
            .and().doNotHaveFullyQualifiedName(PUBLIC_BY_NECESSITY)
            .should().notBePublic()
            .because("infrastructure 모듈의 클래스는 앱 ModuleScanConfig의 문자열 스캔으로만 등록된다 — 다른 패키지가 참조하는 RedisTokenStoreProperties만 예외다(상위 패키지의 RedisModuleConfig가 @EnableConfigurationProperties로 참조한다)")
            .check(moduleClasses);
    }

    @Test
    void publicByNecessityShouldStillBePublic() {
        JavaClasses moduleClasses = new ClassFileImporter()
            .importPath(Path.of("build/classes/java/main"));

        assertThat(moduleClasses.get(PUBLIC_BY_NECESSITY).getModifiers())
            .as("허용 목록의 클래스가 더 이상 public이 아니면 허용 목록에서 지운다")
            .contains(JavaModifier.PUBLIC);
    }
}
