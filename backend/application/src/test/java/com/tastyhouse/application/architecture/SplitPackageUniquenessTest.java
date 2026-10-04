package com.tastyhouse.application.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SplitPackageUniquenessTest {

    private static final Map<String, Path> MODULE_SOURCE_ROOTS = Map.of(
        "application", Path.of("src/main/java"),
        "web-application", Path.of("../web-application/src/main/java"),
        "admin-application", Path.of("../admin-application/src/main/java"),
        "ceo-application", Path.of("../ceo-application/src/main/java"),
        "batch-application", Path.of("../batch-application/src/main/java"));

    @Test
    @DisplayName("같은 FQCN이 application 계층 5모듈 중 두 곳 이상에 있으면 안 된다")
    void classNamesShouldBeUniqueAcrossApplicationModules() {
        Map<String, List<String>> owners = new TreeMap<>();
        for (Map.Entry<String, Path> entry : MODULE_SOURCE_ROOTS.entrySet()) {
            for (String relative : javaSourcesUnder(entry.getValue())) {
                owners.computeIfAbsent(relative, key -> new ArrayList<>()).add(entry.getKey());
            }
        }

        assertThat(owners.size())
            .as("스캔한 소스가 줄면 경로가 틀려 이 규칙이 공허하게 통과할 수 있다")
            .isGreaterThanOrEqualTo(1400);

        List<String> duplicates = owners.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .map(entry -> entry.getKey() + " " + entry.getValue())
            .toList();

        assertThat(duplicates)
            .as("패키지가 같은(split package) 모듈들에 같은 FQCN이 있으면 컴파일은 통과하고 클래스패스 순서로 한쪽이 조용히 가려진다")
            .isEmpty();
    }

    private static List<String> javaSourcesUnder(Path root) {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                .filter(path -> path.toString().endsWith(".java"))
                .map(path -> root.relativize(path).toString())
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("소스 스캔에 실패했다: " + root, e);
        }
    }
}
