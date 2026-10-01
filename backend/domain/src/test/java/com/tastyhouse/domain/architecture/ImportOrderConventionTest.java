package com.tastyhouse.domain.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImportOrderConventionTest {

    private static final String TASTYHOUSE_PREFIX = "com.tastyhouse.";

    private static final Map<String, Integer> TOP_SEGMENT_RANK = Map.ofEntries(
        Map.entry("domain", 1),
        Map.entry("application", 2),
        Map.entry("external", 3),
        Map.entry("infrastructure", 3),
        Map.entry("restclient", 3),
        Map.entry("apicommon", 4),
        Map.entry("logging", 4),
        Map.entry("security", 4),
        Map.entry("adminapi", 5),
        Map.entry("batch", 5),
        Map.entry("ceoapi", 5),
        Map.entry("webapi", 5)
    );

    private static final Set<String> PRESENTATION_SHARED_SEGMENTS = Set.of(
        "common", "config", "exception", "ratelimit", "security"
    );

    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of("build", "bin", ".gradle");

    private static final int MINIMUM_SCANNED_FILES = 3000;

    private static final Comparator<String> IMPORT_ORDER = Comparator
        .comparingInt(ImportOrderConventionTest::group)
        .thenComparingInt(ImportOrderConventionTest::rank)
        .thenComparingInt(ImportOrderConventionTest::presentationSubRank)
        .thenComparing(Comparator.naturalOrder());

    @Test
    @DisplayName("backend 전체 java 파일의 import 블록은 backend/CLAUDE.md의 import 순서 규칙을 따른다")
    void importsFollowConvention() throws IOException {
        Path backendRoot = findBackendRoot();
        List<Path> javaFiles = collectJavaFiles(backendRoot);

        List<String> violations = new ArrayList<>();
        for (Path file : javaFiles) {
            String violation = checkFile(file);
            if (violation != null) {
                violations.add(backendRoot.getParent().relativize(file) + " — " + violation);
            }
        }

        assertThat(javaFiles.size())
            .as("스캔한 java 파일 수가 너무 적다. backend 루트(%s)를 잘못 찾았을 수 있다", backendRoot)
            .isGreaterThan(MINIMUM_SCANNED_FILES);
        assertThat(violations)
            .as("import 순서 위반. backend 디렉터리에서 python3 import_order.py fix 로 재정렬하거나 backend/CLAUDE.md 규칙대로 고친다")
            .isEmpty();
    }

    private static Path findBackendRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("settings.gradle이 있는 backend 루트를 찾지 못했다: " + Path.of("").toAbsolutePath());
    }

    private static List<Path> collectJavaFiles(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                .filter(path -> path.getFileName().toString().endsWith(".java"))
                .filter(path -> isOutsideExcludedDirectories(root.relativize(path)))
                .filter(Files::isRegularFile)
                .sorted(Comparator.naturalOrder())
                .toList();
        }
    }

    private static boolean isOutsideExcludedDirectories(Path relativePath) {
        for (Path segment : relativePath) {
            if (EXCLUDED_DIRECTORIES.contains(segment.toString())) {
                return false;
            }
        }
        return true;
    }

    private static String checkFile(Path file) {
        List<String> lines = Arrays.asList(read(file).split("\n", -1));

        int first = -1;
        int last = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("import ")) {
                if (first < 0) {
                    first = i;
                }
                last = i;
            }
        }
        if (first < 0) {
            return null;
        }

        List<String> actual = lines.subList(first, last + 1);
        List<String> names = new ArrayList<>();
        for (String line : actual) {
            if (line.startsWith("import ")) {
                names.add(stripTrailingSemicolons(line.substring("import ".length()).stripTrailing()));
            } else if (!line.isBlank()) {
                return "import 블록 사이에 import가 아닌 줄: " + line;
            }
        }

        for (String name : names) {
            if (group(name) == 4 && !TOP_SEGMENT_RANK.containsKey(topSegment(name))) {
                return "순위 표에 없는 최상위 세그먼트: " + name
                    + " (backend/CLAUDE.md 순위 표와 이 테스트의 TOP_SEGMENT_RANK에 함께 추가한다)";
            }
        }

        List<String> expected = render(names);
        int size = Math.max(actual.size(), expected.size());
        for (int i = 0; i < size; i++) {
            String actualLine = i < actual.size() ? actual.get(i) : "<없음>";
            String expectedLine = i < expected.size() ? expected.get(i) : "<없음>";
            if (!actualLine.equals(expectedLine)) {
                return "블록 " + (i + 1) + "번째 줄: 기대 [" + expectedLine + "] 실제 [" + actualLine + "]";
            }
        }
        return null;
    }

    private static List<String> render(List<String> names) {
        TreeSet<String> sorted = new TreeSet<>(IMPORT_ORDER);
        sorted.addAll(names);

        List<String> rendered = new ArrayList<>();
        Integer previousGroup = null;
        for (String name : sorted) {
            int currentGroup = group(name);
            if (previousGroup != null && currentGroup != previousGroup) {
                rendered.add("");
            }
            rendered.add("import " + name + ";");
            previousGroup = currentGroup;
        }
        return rendered;
    }

    private static int group(String name) {
        if (name.startsWith("static ")) {
            return 5;
        }
        if (name.startsWith("java.")) {
            return 1;
        }
        if (name.startsWith("javax.")) {
            return 2;
        }
        if (name.startsWith(TASTYHOUSE_PREFIX)) {
            return 4;
        }
        return 3;
    }

    private static int rank(String name) {
        if (group(name) != 4) {
            return 0;
        }
        return TOP_SEGMENT_RANK.getOrDefault(topSegment(name), Integer.MAX_VALUE);
    }

    private static int presentationSubRank(String name) {
        if (rank(name) != 5) {
            return 0;
        }
        String[] parts = name.substring(TASTYHOUSE_PREFIX.length()).split("\\.");
        return parts.length > 2 && PRESENTATION_SHARED_SEGMENTS.contains(parts[1]) ? 0 : 1;
    }

    private static String topSegment(String name) {
        return name.substring(TASTYHOUSE_PREFIX.length()).split("\\.")[0];
    }

    private static String stripTrailingSemicolons(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == ';') {
            end--;
        }
        return value.substring(0, end);
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
