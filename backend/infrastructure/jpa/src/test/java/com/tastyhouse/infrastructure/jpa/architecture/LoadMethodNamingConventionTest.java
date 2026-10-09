package com.tastyhouse.infrastructure.jpa.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoadMethodNamingConventionTest {

    private static final Path SOURCE_ROOT =
        Path.of("src/main/java/com/tastyhouse/infrastructure/jpa");

    private static final Pattern PUBLIC_METHOD =
        Pattern.compile("^    public [^\\n(=]+? (\\w+)\\([^)]*\\)[^{;]*\\{", Pattern.MULTILINE);

    private static final Pattern IMPLICIT_FILTER =
        Pattern.compile("\\b\\w+JpaEntity\\.(?:deleted\\.(?:isFalse\\(\\)|eq\\(false\\))|active\\.(?:isTrue\\(\\)|eq\\(true\\)))");

    private static final Pattern FILTER_QUALIFIER =
        Pattern.compile("Active|Visible");

    private static final Pattern SOFT_DELETE_FIELD =
        Pattern.compile("private (?:boolean|Boolean) deleted\\b");

    private static final int MIN_FILTERED_METHODS = 27;

    private static final int MIN_SOFT_DELETE_ENTITIES = 12;

    private static final int MIN_FIND_BY_ID_METHODS = 40;

    private static final int MIN_SOFT_DELETE_READ_METHODS = 30;

    @Test
    @DisplayName("삭제·활성 필터를 거는 쓰기 어댑터 조회는 이름에 Active 또는 Visible을 담는다")
    void filteredLoadMethodsShouldNameTheFilter() {
        List<String> violations = new ArrayList<>();
        int filtered = 0;

        for (Path source : adapterSources()) {
            String text = read(source);
            for (MethodBody method : publicMethods(text)) {
                if (!IMPLICIT_FILTER.matcher(method.body()).find()) {
                    continue;
                }
                filtered++;
                if (!FILTER_QUALIFIER.matcher(method.name()).find()) {
                    violations.add(location(source, text, method.offset()) + " " + method.name());
                }
            }
        }

        assertThat(filtered).isGreaterThanOrEqualTo(MIN_FILTERED_METHODS);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("소프트 삭제 엔티티를 읽는 쓰기 어댑터는 수식어 없는 findById를 공개하지 않는다")
    void softDeleteEntitiesShouldNotExposeBareFindById() {
        List<String> softDeleteEntities = softDeleteEntityNames();
        assertThat(softDeleteEntities).hasSizeGreaterThanOrEqualTo(MIN_SOFT_DELETE_ENTITIES);

        List<Pattern> softDeleteReads = new ArrayList<>();
        for (String entity : softDeleteEntities) {
            String prefix = Character.toLowerCase(entity.charAt(0)) + entity.substring(1, entity.length() - "JpaEntity".length());
            softDeleteReads.add(Pattern.compile("\\b" + prefix + "JpaRepository\\.findById\\("));
            softDeleteReads.add(Pattern.compile("selectFrom\\(\\s*" + prefix + "JpaEntity\\s*\\)"));
        }

        List<String> violations = new ArrayList<>();
        int findByIdMethods = 0;
        int softDeleteReadMethods = 0;
        for (Path source : adapterSources()) {
            String text = read(source);
            for (MethodBody method : publicMethods(text)) {
                boolean readsSoftDeleteEntity = softDeleteReads.stream()
                    .anyMatch(read -> read.matcher(method.body()).find());
                if (readsSoftDeleteEntity) {
                    softDeleteReadMethods++;
                }
                if (!method.name().equals("findById")) {
                    continue;
                }
                findByIdMethods++;
                if (readsSoftDeleteEntity) {
                    violations.add(location(source, text, method.offset()));
                }
            }
        }

        assertThat(findByIdMethods).isGreaterThanOrEqualTo(MIN_FIND_BY_ID_METHODS);
        assertThat(softDeleteReadMethods).isGreaterThanOrEqualTo(MIN_SOFT_DELETE_READ_METHODS);
        assertThat(violations).isEmpty();
    }

    private List<String> softDeleteEntityNames() {
        List<String> names = new ArrayList<>();
        for (Path source : javaSources()) {
            String fileName = source.getFileName().toString();
            if (fileName.endsWith("JpaEntity.java") && SOFT_DELETE_FIELD.matcher(read(source)).find()) {
                names.add(fileName.substring(0, fileName.length() - ".java".length()));
            }
        }
        return names;
    }

    private List<MethodBody> publicMethods(String text) {
        List<MethodBody> methods = new ArrayList<>();
        Matcher matcher = PUBLIC_METHOD.matcher(text);
        while (matcher.find()) {
            int depth = 1;
            int index = matcher.end();
            while (depth > 0 && index < text.length()) {
                char c = text.charAt(index++);
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                }
            }
            methods.add(new MethodBody(matcher.group(1), text.substring(matcher.end(), index), matcher.start()));
        }
        return methods;
    }

    private List<Path> adapterSources() {
        return javaSources().stream()
            .filter(path -> path.getFileName().toString().endsWith("PersistenceAdapter.java"))
            .toList();
    }

    private String location(Path source, String text, int offset) {
        int line = 1;
        for (int i = 0; i < offset; i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return source + ":" + line;
    }

    private List<Path> javaSources() {
        try (Stream<Path> paths = Files.walk(SOURCE_ROOT)) {
            List<Path> sources = paths.filter(path -> path.toString().endsWith(".java")).toList();
            assertThat(sources).isNotEmpty();
            return sources;
        } catch (IOException e) {
            throw new UncheckedIOException("infrastructure 소스 스캔에 실패했다: " + SOURCE_ROOT, e);
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException("소스 파일을 읽지 못했다: " + path, e);
        }
    }

    private record MethodBody(String name, String body, int offset) {
    }
}
