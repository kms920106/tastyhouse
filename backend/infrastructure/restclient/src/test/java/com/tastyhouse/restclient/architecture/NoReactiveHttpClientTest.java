package com.tastyhouse.restclient.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoReactiveHttpClientTest {

    private static final Path INFRASTRUCTURE_ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final Path GUARD_SOURCE = INFRASTRUCTURE_ROOT.resolve(
        "restclient/src/test/java/com/tastyhouse/restclient/architecture/NoReactiveHttpClientTest.java"
    );
    private static final List<String> FORBIDDEN_TOKENS = List.of(
        "org.springframework.web.reactive",
        "reactor.",
        "java.net.http."
    );
    private static final int MINIMUM_SOURCE_COUNT = 50;

    @Test
    @DisplayName("infrastructure 모듈 소스는 webflux·reactor·JDK HttpClient를 참조하지 않는다")
    void noReactiveOrJdkHttpClientReferences() {
        List<Path> sources = javaSources();
        assertThat(sources).hasSizeGreaterThan(MINIMUM_SOURCE_COUNT);
        assertThat(sources).contains(GUARD_SOURCE);

        List<String> violations = new ArrayList<>();
        for (Path source : sources) {
            if (source.equals(GUARD_SOURCE)) {
                continue;
            }
            for (String line : readLines(source)) {
                if (containsForbiddenToken(line)) {
                    violations.add(INFRASTRUCTURE_ROOT.relativize(source) + ": " + line.trim());
                }
            }
        }

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("infrastructure 모듈 build.gradle은 spring-boot-starter-webflux를 선언하지 않는다")
    void noWebfluxDependency() {
        List<Path> buildFiles = buildGradleFiles();
        assertThat(buildFiles).hasSizeGreaterThan(1);

        List<Path> violations = buildFiles.stream()
            .filter(path -> String.join("\n", readLines(path)).contains("spring-boot-starter-webflux"))
            .map(INFRASTRUCTURE_ROOT::relativize)
            .toList();

        assertThat(violations).isEmpty();
    }

    private static boolean containsForbiddenToken(String line) {
        return FORBIDDEN_TOKENS.stream().anyMatch(line::contains);
    }

    private static List<Path> javaSources() {
        try (Stream<Path> paths = Files.walk(INFRASTRUCTURE_ROOT)) {
            return paths
                .map(Path::normalize)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(NoReactiveHttpClientTest::isUnderSourceSet)
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<Path> buildGradleFiles() {
        try (Stream<Path> paths = Files.list(INFRASTRUCTURE_ROOT)) {
            return paths
                .map(moduleDir -> moduleDir.resolve("build.gradle"))
                .filter(Files::isRegularFile)
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isUnderSourceSet(Path path) {
        Path relative = INFRASTRUCTURE_ROOT.relativize(path);
        return relative.getNameCount() > 2 && relative.getName(1).toString().equals("src");
    }

    private static List<String> readLines(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
