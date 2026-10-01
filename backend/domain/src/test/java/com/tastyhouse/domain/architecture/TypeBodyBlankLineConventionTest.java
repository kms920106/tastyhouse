package com.tastyhouse.domain.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypeBodyBlankLineConventionTest {

    private static final Set<String> TYPE_KEYWORDS = Set.of("class", "interface", "enum", "record");

    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of("build", "bin", ".gradle");

    private static final int MINIMUM_SCANNED_FILES = 3000;

    @Test
    @DisplayName("backend 전체 java 파일의 타입 본문 첫 줄은 빈 줄 정확히 1개다")
    void typeBodiesStartWithSingleBlankLine() throws IOException {
        Path backendRoot = findBackendRoot();
        List<Path> javaFiles = collectJavaFiles(backendRoot);

        List<String> violations = new ArrayList<>();
        for (Path file : javaFiles) {
            for (int line : violatingLines(read(file))) {
                violations.add(backendRoot.getParent().relativize(file) + ":" + line);
            }
        }

        assertThat(javaFiles.size())
            .as("스캔한 java 파일 수가 너무 적다. backend 루트(%s)를 잘못 찾았을 수 있다", backendRoot)
            .isGreaterThan(MINIMUM_SCANNED_FILES);
        assertThat(violations)
            .as("타입 본문 여는 중괄호 다음 줄은 빈 줄 정확히 1개여야 한다. "
                + "IntelliJ Code Style > Java > Blank Lines > After class header: 1 로 설정 후 reformat 하거나 "
                + "backend/CLAUDE.md 타입 본문 첫 줄 빈 줄 규칙대로 고친다")
            .isEmpty();
    }

    @Test
    @DisplayName("빈 줄이 없거나 2개 이상이면 위반이다")
    void missingOrDoubleBlankLineIsViolation() {
        assertThat(violatingLines("class A {\n    int x;\n}\n")).containsExactly(1);
        assertThat(violatingLines("class A {\n\n\n    int x;\n}\n")).containsExactly(1);
    }

    @Test
    @DisplayName("빈 줄 1개면 준수다")
    void singleBlankLineIsCompliant() {
        assertThat(violatingLines("public class A {\n\n    int x;\n}\n")).isEmpty();
        assertThat(violatingLines("public interface A {\n\n    void run();\n}\n")).isEmpty();
        assertThat(violatingLines("enum A {\n\n    X, Y\n}\n")).isEmpty();
        assertThat(violatingLines("public @interface A {\n\n    String value();\n}\n")).isEmpty();
    }

    @Test
    @DisplayName("여러 줄 헤더·어노테이션 배열·sealed permits·중첩 제네릭 뒤의 본문도 판정한다")
    void complexHeadersAreChecked() {
        assertThat(violatingLines("public record A(\n    @Schema(example = \"{x}\") String a,\n    List<Map<String, Integer>> b\n) {\n    static A of() { return null; }\n}\n"))
            .containsExactly(4);
        assertThat(violatingLines("@Target({ElementType.TYPE})\npublic @interface A {\n    String value();\n}\n"))
            .containsExactly(2);
        assertThat(violatingLines("public sealed interface A permits B, C {\n    void run();\n}\n"))
            .containsExactly(1);
        assertThat(violatingLines("class A implements Function<List<String>, Map<String, List<Integer>>> {\n    int x;\n}\n"))
            .containsExactly(1);
    }

    @Test
    @DisplayName("중첩·로컬 타입도 판정한다")
    void nestedTypesAreChecked() {
        String source = "class A {\n\n    static class B {\n        int x;\n    }\n\n    void m() {\n        record C(int x) {\n            static int y = 0;\n        }\n    }\n}\n";
        assertThat(violatingLines(source)).containsExactly(3, 8);
    }

    @Test
    @DisplayName("빈 본문·한 줄 본문·익명 클래스·enum 상수 본문은 제외한다")
    void exemptBodiesAreIgnored() {
        assertThat(violatingLines("class A {}\n")).isEmpty();
        assertThat(violatingLines("record A(int x) {\n}\n")).isEmpty();
        assertThat(violatingLines("class A {\n\n}\n")).isEmpty();
        assertThat(violatingLines("class A {\n\n    enum Status {LOGIN, NEEDS_SIGN_UP}\n}\n")).isEmpty();
        assertThat(violatingLines("class A {\n\n    Runnable r = new Runnable() {\n        public void run() {}\n    };\n}\n")).isEmpty();
        assertThat(violatingLines("enum A {\n\n    X {\n        int f() { return 1; }\n    };\n}\n")).isEmpty();
    }

    @Test
    @DisplayName("타입 선언이 아닌 키워드 사용은 판정 대상이 아니다")
    void nonDeclarationKeywordUsesAreIgnored() {
        String source = "class A {\n\n"
            + "    Class<?> c = String.class;\n"
            + "    Class<?> d = String .class;\n"
            + "    public void record(Object o) {\n        o.hashCode();\n    }\n"
            + "    void enumsAreStoredAsNames() {\n        int classes = 0;\n    }\n"
            + "    void m(java.util.Map<String, String> markers) {\n"
            + "        markers.forEach((record, value) -> {\n            value.length();\n        });\n"
            + "        for (String record : markers.keySet()) {\n            record.length();\n        }\n"
            + "    }\n"
            + "}\n";
        assertThat(violatingLines(source)).isEmpty();
    }

    @Test
    @DisplayName("문자열·문자·텍스트 블록·주석 안의 중괄호와 키워드는 무시한다")
    void literalsAndCommentsAreIgnored() {
        String source = "class A {\n\n"
            + "    String s = \"class X {\";\n"
            + "    char c = '{';\n"
            + "    char q = '\"';\n"
            + "    char e = '\\'';\n"
            + "    String t = \"\"\"\n        class Y {\n        int z;\n        }\n        \"\"\";\n"
            + "    // class Z {\n"
            + "}\n";
        assertThat(violatingLines(source)).isEmpty();

        String escapedTextBlock = "class A {\n\n"
            + "    String t = \"\"\"\n        quote \\\"\"\" class W {\n        \"\"\";\n"
            + "    static class B {\n        int x;\n    }\n"
            + "}\n";
        assertThat(violatingLines(escapedTextBlock)).containsExactly(6);
    }

    static List<Integer> violatingLines(String source) {
        String[] lines = source.split("\n", -1);
        List<Integer> violations = new ArrayList<>();
        for (int[] brace : typeBodyBraces(source)) {
            if (isViolation(lines, brace[0], brace[1])) {
                violations.add(brace[0] + 1);
            }
        }
        return violations;
    }

    private static boolean isViolation(String[] lines, int lineIndex, int column) {
        if (!lines[lineIndex].substring(column + 1).isBlank()) {
            return false;
        }
        int next = lineIndex + 1;
        while (next < lines.length && lines[next].isBlank()) {
            next++;
        }
        if (next < lines.length && lines[next].strip().equals("}")) {
            return false;
        }
        return next - lineIndex - 1 != 1;
    }

    private static List<int[]> typeBodyBraces(String source) {
        List<int[]> braces = new ArrayList<>();
        int length = source.length();
        int line = 0;
        int lineStart = 0;
        boolean pending = false;
        boolean needName = false;
        int depth = 0;
        String previous = "";
        int i = 0;
        while (i < length) {
            char ch = source.charAt(i);
            if (ch == '\n') {
                line++;
                i++;
                lineStart = i;
                continue;
            }
            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }
            if (source.startsWith("\"\"\"", i)) {
                int end = i + 3;
                while (end < length && !source.startsWith("\"\"\"", end)) {
                    end += source.charAt(end) == '\\' ? 2 : 1;
                }
                end = Math.min(end + 3, length);
                for (int k = i; k < end; k++) {
                    if (source.charAt(k) == '\n') {
                        line++;
                        lineStart = k + 1;
                    }
                }
                i = end;
                previous = "literal";
                continue;
            }
            if (ch == '"' || ch == '\'') {
                i++;
                while (i < length && source.charAt(i) != ch) {
                    i += source.charAt(i) == '\\' ? 2 : 1;
                }
                i++;
                previous = "literal";
                continue;
            }
            if (source.startsWith("//", i)) {
                int end = source.indexOf('\n', i);
                i = end < 0 ? length : end;
                continue;
            }
            if (source.startsWith("/*", i)) {
                int end = source.indexOf("*/", i + 2);
                end = end < 0 ? length : end + 2;
                for (int k = i; k < end; k++) {
                    if (source.charAt(k) == '\n') {
                        line++;
                        lineStart = k + 1;
                    }
                }
                i = end;
                continue;
            }
            if (ch == '@' && source.startsWith("interface", i + 1)
                && (i + 10 >= length || !Character.isJavaIdentifierPart(source.charAt(i + 10)))) {
                needName = true;
                previous = "@interface";
                i += 10;
                continue;
            }
            if (Character.isJavaIdentifierStart(ch)) {
                int end = i + 1;
                while (end < length && Character.isJavaIdentifierPart(source.charAt(end))) {
                    end++;
                }
                String word = source.substring(i, end);
                if (needName) {
                    needName = false;
                    pending = true;
                    depth = 0;
                } else if (TYPE_KEYWORDS.contains(word) && !previous.equals(".")) {
                    needName = true;
                }
                previous = word;
                i = end;
                continue;
            }
            needName = false;
            if (pending) {
                if (ch == '(' || ch == '<') {
                    depth++;
                } else if (ch == ')' || ch == '>') {
                    depth--;
                    if (depth < 0) {
                        pending = false;
                    }
                } else if (ch == ';' && depth == 0) {
                    pending = false;
                } else if (ch == '{' && depth == 0) {
                    braces.add(new int[] {line, i - lineStart});
                    pending = false;
                }
            }
            previous = String.valueOf(ch);
            i++;
        }
        return braces;
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

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
