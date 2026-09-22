package com.tastyhouse.application.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ResultWitherComponentOrderTest {
    private static final Path APPLICATION_SOURCE_ROOT =
        Path.of("src/main/java/com/tastyhouse/application");

    private static final Pattern PACKAGE_DECLARATION =
        Pattern.compile("^package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_$][\\w$]*");

    private static final Pattern FIELD_REFERENCE = Pattern.compile("this\\.([A-Za-z_$][\\w$]*)");

    @Test
    @DisplayName("port.out Result record의 wither는 record 컴포넌트 순서대로 인자를 재나열해야 한다")
    void witherArgumentsShouldFollowComponentOrder() {
        List<String> mismatches = new ArrayList<>();
        int checked = 0;

        for (Path source : portOutSources()) {
            String text = read(source);
            String recordName = source.getFileName().toString().replace(".java", "");
            Class<?> target = load(packageOf(text) + "." + recordName);
            if (target == null) {
                if (text.contains("record " + recordName)) {
                    mismatches.add("%s: record 클래스를 로드하지 못했다".formatted(recordName));
                }
                continue;
            }
            if (!target.isRecord()) {
                continue;
            }
            int parsedWithers = 0;

            Matcher signature = Pattern
                .compile("public\\s+" + Pattern.quote(recordName) + "\\s+(with\\w+)\\(")
                .matcher(text);
            while (signature.find()) {
                parsedWithers++;
                String witherName = signature.group(1);
                List<String> parameters = readArguments(text, signature.end(), true);
                if (parameters == null) {
                    mismatches.add("%s#%s: 시그니처를 해석하지 못했다".formatted(recordName, witherName));
                    continue;
                }
                List<String> parameterNames = parameters.stream()
                    .map(String::strip)
                    .filter(parameter -> !parameter.isEmpty())
                    .map(parameter -> parameter.split("\\s+"))
                    .map(tokens -> tokens[tokens.length - 1])
                    .toList();

                Matcher construction = Pattern
                    .compile("new\\s+" + Pattern.quote(recordName) + "\\s*\\(")
                    .matcher(text);
                if (!construction.find(signature.end())) {
                    mismatches.add("%s#%s: new %s(...)를 찾지 못했다".formatted(recordName, witherName, recordName));
                    continue;
                }
                List<String> arguments = readArguments(text, construction.end(), false);
                if (arguments == null) {
                    mismatches.add("%s#%s: new 인자를 해석하지 못했다".formatted(recordName, witherName));
                    continue;
                }

                checked++;
                String reordering = detectWitherReordering(target, arguments, parameterNames);
                if (reordering != null) {
                    mismatches.add("%s#%s: %s".formatted(recordName, witherName, reordering));
                }
            }

            long declaredWithers = declaredWithersOf(target);
            if (declaredWithers != parsedWithers) {
                mismatches.add("%s: 선언된 wither %d개 중 %d개만 파싱했다 — 시그니처 패턴이 놓친 wither가 있다"
                    .formatted(recordName, declaredWithers, parsedWithers));
            }
        }

        assertThat(checked)
            .as("port/out wither를 하나도 찾지 못했다 — 스캔 경로(%s)나 패턴이 잘못되었을 수 있다",
                APPLICATION_SOURCE_ROOT)
            .isPositive();

        assertThat(mismatches)
            .as("""
                아래 wither의 new 인자가 record 컴포넌트 순서와 어긋난다. wither는 전 컴포넌트를
                위치 기반으로 재나열하므로 인접한 동일 타입 컴포넌트가 뒤바뀌어도 컴파일되고,
                응답에 다른 필드의 값이 조용히 실린다. record 선언 순서와 인자 순서를 하나씩 대조하라.""")
            .isEmpty();
    }

    @Test
    @DisplayName("순서 검출기는 this. 참조·필드 참조·파라미터 슬롯의 교차를 각각 잡아낸다")
    void detectorShouldCatchSwappedSlots() {
        List<String> parameters = List.of("imageUrls");

        assertThat(detectWitherReordering(SwapProbe.class,
            List.of("this.id", "imageUrls", "this.productNames"), parameters)).isNull();
        assertThat(detectWitherReordering(SwapProbe.class,
            List.of("id", "imageUrls", "productNames"), parameters)).isNull();

        assertThat(detectWitherReordering(SwapProbe.class,
            List.of("this.id", "this.productNames", "imageUrls"), parameters))
            .contains("1번 자리에 this.productNames", "2번 자리에 파라미터 imageUrls");
        assertThat(detectWitherReordering(SwapProbe.class,
            List.of("id", "productNames", "imageUrls"), parameters))
            .contains("1번 자리에 productNames", "2번 자리에 파라미터 imageUrls");
        assertThat(detectWitherReordering(SwapProbe.class,
            List.of("this.id", "imageUrls"), parameters))
            .contains("new 인자 2개 ↔ record 컴포넌트 3개");

        assertThat(detectWitherReordering(SwapProbe.class,
            List.of("this.id", "this.imageUrls", "this.productNames"), parameters))
            .contains("파라미터 imageUrls를 인자로 쓰지 않는다");
        assertThat(detectWitherReordering(SwapProbe.class,
            List.of("this.id", "this.imageUrls.subList(0, 1)", "this.productNames"), List.of()))
            .isNull();
    }

    record SwapProbe(Long id, List<String> imageUrls, List<String> productNames) {
    }

    private String detectWitherReordering(Class<?> target, List<String> arguments, List<String> parameterNames) {
        List<String> components = Arrays.stream(target.getRecordComponents())
            .map(RecordComponent::getName)
            .toList();
        if (components.size() != arguments.size()) {
            return "wither의 new 인자 %d개 ↔ record 컴포넌트 %d개"
                .formatted(arguments.size(), components.size());
        }

        List<String> swapped = new ArrayList<>();
        for (int i = 0; i < arguments.size(); i++) {
            String argument = arguments.get(i).strip();
            String expected = components.get(i);

            Matcher fieldReference = FIELD_REFERENCE.matcher(argument);
            if (fieldReference.matches()) {
                String name = fieldReference.group(1);
                if (!name.equals(expected)) {
                    swapped.add("%d번 자리에 this.%s (기대: %s)".formatted(i, name, expected));
                }
                continue;
            }
            if (!IDENTIFIER.matcher(argument).matches()) {
                continue;
            }
            if (parameterNames.contains(argument)) {
                if (components.contains(argument) && !argument.equals(expected)) {
                    swapped.add("%d번 자리에 파라미터 %s (기대: %s)".formatted(i, argument, expected));
                }
                continue;
            }
            if (components.contains(argument) && !argument.equals(expected)) {
                swapped.add("%d번 자리에 %s (기대: %s)".formatted(i, argument, expected));
            }
        }
        List<String> strippedArguments = arguments.stream().map(String::strip).toList();
        for (String parameter : parameterNames) {
            if (!strippedArguments.contains(parameter)) {
                swapped.add("파라미터 %s를 인자로 쓰지 않는다 (기존 값이 그대로 반환된다)".formatted(parameter));
            }
        }
        return swapped.isEmpty() ? null : String.join(", ", swapped);
    }

    private long declaredWithersOf(Class<?> target) {
        return Arrays.stream(target.getDeclaredMethods())
            .filter(method -> !method.isSynthetic())
            .filter(method -> !Modifier.isStatic(method.getModifiers()))
            .filter(method -> method.getName().matches("with[A-Z]\\w*"))
            .map(Method::getReturnType)
            .filter(target::equals)
            .count();
    }

    private List<String> readArguments(String text, int afterOpeningParenthesis, boolean genericsNest) {
        List<String> arguments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 1;
        boolean inString = false;
        boolean inChar = false;

        for (int i = afterOpeningParenthesis; i < text.length(); i++) {
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
                case '<' -> {
                    if (genericsNest) {
                        depth++;
                    }
                }
                case '>' -> {
                    if (genericsNest) {
                        depth--;
                    }
                }
                case ')' -> {
                    depth--;
                    if (depth == 0) {
                        if (!current.toString().isBlank() || !arguments.isEmpty()) {
                            arguments.add(current.toString());
                        }
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

    private String packageOf(String text) {
        Matcher matcher = PACKAGE_DECLARATION.matcher(text);
        return matcher.find() ? matcher.group(1) : "";
    }

    private Class<?> load(String fullyQualifiedName) {
        try {
            return ClassUtils.resolveClassName(fullyQualifiedName, getClass().getClassLoader());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private List<Path> portOutSources() {
        try (Stream<Path> paths = Files.walk(APPLICATION_SOURCE_ROOT)) {
            return paths
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> path.getParent().endsWith(Path.of("port", "out")))
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("application 소스 스캔에 실패했다: " + APPLICATION_SOURCE_ROOT, e);
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException("소스 파일을 읽지 못했다: " + path, e);
        }
    }
}
