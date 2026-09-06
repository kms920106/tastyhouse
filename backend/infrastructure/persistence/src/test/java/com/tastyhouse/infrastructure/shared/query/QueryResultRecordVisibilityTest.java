package com.tastyhouse.infrastructure.shared.query;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

class QueryResultRecordVisibilityTest {
    private static final List<String> QUERY_PACKAGE_PATTERNS = List.of(
        "classpath*:com/tastyhouse/infrastructure/**/query/*.class",
        "classpath*:com/tastyhouse/application/**/port/out/*.class");

    @Test
    @DisplayName("query 패키지의 Result record는 public이어야 한다 (QueryDSL Projections.constructor 탐색 대상)")
    void queryResultRecordsShouldBePublic() {
        List<Class<?>> resultRecords = findQueryPackageRecords();

        assertThat(resultRecords)
            .as("query 패키지에서 record를 하나도 찾지 못했다 — 스캔 패턴(%s)이 잘못되었을 수 있다",
                QUERY_PACKAGE_PATTERNS)
            .isNotEmpty();

        List<String> nonPublicRecords = resultRecords.stream()
            .filter(recordType -> !Modifier.isPublic(recordType.getModifiers()))
            .map(Class::getName)
            .toList();

        assertThat(nonPublicRecords)
            .as("""
                아래 record가 public이 아니다. QueryDSL의 Projections.constructor는 생성자를
                Class#getConstructors()로 찾는데 이 메서드는 public 생성자만 반환하므로, package-private
                record는 같은 패키지에서 투영하더라도 런타임에 ExpressionException(No constructor found)으로
                실패한다 — 컴파일은 통과하기 때문에 이 테스트 외에는 걸러낼 방법이 없다.
                DAO 내부 전용 중간 투영이더라도 public record로 선언한다.""")
            .isEmpty();
    }

    private List<Class<?>> findQueryPackageRecords() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);

        List<Class<?>> records = new ArrayList<>();
        try {
            for (Resource resource : resolveAll(resolver)) {
                String className = metadataReaderFactory.getMetadataReader(resource)
                    .getClassMetadata()
                    .getClassName();
                if (className.contains("$")) {
                    continue;
                }
                Class<?> type = ClassUtils.resolveClassName(className, getClass().getClassLoader());
                if (type.isRecord()) {
                    records.add(type);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("query 패키지 클래스 스캔에 실패했다: " + QUERY_PACKAGE_PATTERNS, e);
        }

        return records;
    }

    private List<Resource> resolveAll(PathMatchingResourcePatternResolver resolver) throws IOException {
        List<Resource> resources = new ArrayList<>();
        for (String pattern : QUERY_PACKAGE_PATTERNS) {
            resources.addAll(List.of(resolver.getResources(pattern)));
        }
        return resources;
    }
}
