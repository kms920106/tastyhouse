package com.tastyhouse.infrastructure.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectionConstructorMatchingTest {
    private static final Path QUERY_SOURCE_ROOT =
        Path.of("src/main/java/com/tastyhouse/infrastructure");

    private static final Pattern CALL_START =
        Pattern.compile("Projections\\.constructor\\(\\s*([A-Za-z0-9_.]+)\\.class\\s*,");

    @Test
    @DisplayName("Projections.constructor 인자 개수가 대상 record 생성자 파라미터 개수와 일치해야 한다")
    void projectionArgumentCountShouldMatchConstructor() {
        List<String> mismatches = new ArrayList<>();
        int checked = 0;

        for (Path source : javaSources()) {
            String text = read(source);
            List<String> imports = importsOf(text);
            Matcher matcher = CALL_START.matcher(text);

            while (matcher.find()) {
                String simpleName = matcher.group(1);
                Class<?> target = resolve(simpleName, imports);
                if (target == null || !target.isRecord()) {
                    continue;
                }

                List<String> arguments = readArguments(text, matcher.end());
                if (arguments == null) {
                    continue;
                }
                int actual = arguments.size();

                List<Integer> accepted = publicConstructorArities(target);
                checked++;
                if (!accepted.contains(actual)) {
                    mismatches.add("%s: Projections.constructor(%s.class, ...) 인자 %d개 ↔ public 생성자 파라미터 %s"
                        .formatted(source.getFileName(), simpleName, actual, accepted));
                    continue;
                }

                String reordering = detectReordering(target, arguments);
                if (reordering != null) {
                    mismatches.add("%s: Projections.constructor(%s.class, ...) %s"
                        .formatted(source.getFileName(), simpleName, reordering));
                }
            }
        }

        assertThat(checked)
            .as("Projections.constructor 호출을 하나도 찾지 못했다 — 스캔 경로(%s)나 패턴이 잘못되었을 수 있다",
                QUERY_SOURCE_ROOT)
            .isPositive();

        assertThat(mismatches)
            .as("""
                아래 select 절의 인자 개수가 대상 record의 생성자와 다르다. Projections.constructor는
                Class<?>와 가변인자를 받으므로 컴파일이 통과하고, 해당 조회가 실행될 때 QueryDSL
                ExpressionException(No constructor found)으로 500이 난다 — 이 테스트 외에는 걸러낼 방법이 없다.
                record 컴포넌트 선언 순서와 select 절 인자 순서를 하나씩 대조하라.""")
            .isEmpty();
    }

    private List<String> readArguments(String text, int afterFirstComma) {
        List<String> arguments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 1;
        boolean inString = false;
        boolean inChar = false;

        for (int i = afterFirstComma; i < text.length(); i++) {
            char c = text.charAt(i);
            char prev = text.charAt(i - 1);

            if (inString) {
                if (c == '"' && prev != '\\') {
                    inString = false;
                }
                continue;
            }
            if (inChar) {
                if (c == '\'' && prev != '\\') {
                    inChar = false;
                }
                continue;
            }

            switch (c) {
                case '"' -> inString = true;
                case '\'' -> inChar = true;
                case '(' -> depth++;
                case ')' -> {
                    depth--;
                    if (depth == 0) {
                        arguments.add(current.toString());
                        return arguments;
                    }
                }
                case ',' -> {
                    if (depth == 1) {
                        arguments.add(current.toString());
                        current.setLength(0);
                        continue;
                    }
                }
                default -> { }
            }
            current.append(c);
        }
        return null;
    }

    private String detectReordering(Class<?> target, List<String> arguments) {
        List<String> components = java.util.Arrays.stream(target.getRecordComponents())
            .map(java.lang.reflect.RecordComponent::getName)
            .toList();
        if (components.size() != arguments.size()) {
            return null;
        }

        List<String> argumentNames = arguments.stream().map(this::trailingPropertyName).toList();

        List<String> swapped = new ArrayList<>();
        for (int i = 0; i < argumentNames.size(); i++) {
            String name = argumentNames.get(i);
            if (name == null || name.equals(components.get(i))) {
                continue;
            }
            if (java.util.Collections.frequency(argumentNames, name) != 1
                || java.util.Collections.frequency(components, name) != 1) {
                continue;
            }
            int expectedIndex = components.indexOf(name);
            if (expectedIndex >= 0 && argumentNames.get(expectedIndex) != null) {
                swapped.add("%s: select %d번 자리 ↔ record %d번 컴포넌트".formatted(name, i, expectedIndex));
            }
        }
        if (swapped.isEmpty()) {
            return null;
        }

        return "인자 순서가 record 컴포넌트 순서와 어긋난다 — " + String.join(", ", swapped)
            + " (select=%s ↔ record=%s)".formatted(argumentNames, components);
    }

    private String trailingPropertyName(String argument) {
        String trimmed = argument.strip();
        if (!trimmed.matches("[A-Za-z_$][\\w$]*(\\.[A-Za-z_$][\\w$]*)+")) {
            return null;
        }
        return trimmed.substring(trimmed.lastIndexOf('.') + 1);
    }

    private List<Integer> publicConstructorArities(Class<?> target) {
        List<Integer> arities = new ArrayList<>();
        for (Constructor<?> constructor : target.getConstructors()) {
            arities.add(constructor.getParameterCount());
        }
        return arities;
    }

    private Class<?> resolve(String simpleName, List<String> imports) {
        if (simpleName.contains(".")) {
            return load(simpleName);
        }
        for (String imported : imports) {
            if (imported.endsWith("." + simpleName)) {
                return load(imported);
            }
        }
        return null;
    }

    private Class<?> load(String fullyQualifiedName) {
        try {
            return ClassUtils.resolveClassName(fullyQualifiedName, getClass().getClassLoader());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private List<String> importsOf(String text) {
        return text.lines()
            .filter(line -> line.startsWith("import ") && !line.startsWith("import static "))
            .map(line -> line.substring("import ".length()).replace(";", "").trim())
            .toList();
    }

    private List<Path> javaSources() {
        try (Stream<Path> paths = Files.walk(QUERY_SOURCE_ROOT)) {
            return paths.filter(path -> path.toString().endsWith(".java")).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("infrastructure 소스 스캔에 실패했다: " + QUERY_SOURCE_ROOT, e);
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException("소스 파일을 읽지 못했다: " + path, e);
        }
    }

    @SuppressWarnings("unused")
    private static boolean isPublic(Class<?> type) {
        return Modifier.isPublic(type.getModifiers());
    }
}
