package com.tastyhouse.application.architecture;

import java.net.URI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.architecture.ModuleOrigin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModuleOriginTest {

    @Test
    @DisplayName("클래스 디렉터리 출처는 build 바로 앞 경로 세그먼트로 모듈을 판정한다")
    void classesDirectory() {
        URI core = URI.create("file:/repo/backend/application/build/classes/java/main/com/tastyhouse/application/A.class");
        URI web = URI.create("file:/repo/backend/web-application/build/classes/java/main/com/tastyhouse/application/A.class");

        assertThat(ModuleOrigin.of(core)).isEqualTo(ModuleOrigin.CORE);
        assertThat(ModuleOrigin.of(web)).isEqualTo(ModuleOrigin.WEB);
    }

    @Test
    @DisplayName("jar 출처는 버전 앞까지의 파일명으로 모듈을 판정한다")
    void jarFile() {
        URI core = URI.create("jar:file:/repo/backend/application/build/libs/application-0.0.1-SNAPSHOT.jar!/com/tastyhouse/application/A.class");
        URI batch = URI.create("jar:file:/repo/backend/batch-application/build/libs/batch-application-0.0.1-SNAPSHOT.jar!/com/tastyhouse/application/A.class");

        assertThat(ModuleOrigin.of(core)).isEqualTo(ModuleOrigin.CORE);
        assertThat(ModuleOrigin.of(batch)).isEqualTo(ModuleOrigin.BATCH);
    }

    @Test
    @DisplayName("판정할 수 없는 출처는 조용히 넘기지 않고 실패한다")
    void unknownLocation() {
        URI unknown = URI.create("file:/repo/backend/application/out/production/com/tastyhouse/application/A.class");

        assertThatThrownBy(() -> ModuleOrigin.of(unknown)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("testFixtures 산출물(jar·클래스 디렉터리)은 main 산출물이 아니므로 core로 오판하지 않고 실패한다")
    void testFixturesOutput() {
        URI fixturesJar = URI.create("jar:file:/repo/backend/application/build/libs/application-0.0.1-SNAPSHOT-test-fixtures.jar!/com/tastyhouse/architecture/A.class");
        URI fixturesDirectory = URI.create("file:/repo/backend/application/build/classes/java/testFixtures/com/tastyhouse/architecture/A.class");

        assertThatThrownBy(() -> ModuleOrigin.of(fixturesJar)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ModuleOrigin.of(fixturesDirectory)).isInstanceOf(IllegalStateException.class);
    }
}
