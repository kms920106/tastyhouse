package com.tastyhouse.infrastructure.shared.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddedRecordComponentOrderTest {
    private static final String ENTITY_BASE_PACKAGE = "com.tastyhouse.infrastructure";

    @Test
    @DisplayName("@Embedded record VO의 컴포넌트 선언 순서는 이름 알파벳 오름차순이어야 한다")
    void embeddedRecordComponentsShouldBeInAlphabeticalOrder() {
        List<Class<?>> embeddedRecords = findEmbeddedRecordTypes();

        assertThat(embeddedRecords)
            .as("@Embedded record VO를 하나도 찾지 못했다 — 스캔 대상 패키지(%s)가 잘못되었을 수 있다", ENTITY_BASE_PACKAGE)
            .isNotEmpty();

        for (Class<?> recordType : embeddedRecords) {
            List<String> declaredOrder = Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();
            List<String> alphabeticalOrder = declaredOrder.stream()
                .sorted(Comparator.naturalOrder())
                .toList();

            assertThat(declaredOrder)
                .as("""
                    %s의 record 컴포넌트 선언 순서가 알파벳순이 아니다.
                    Hibernate가 embeddable 프로퍼티를 이름순으로 정렬해 값을 읽은 뒤 canonical 생성자에 위치 기반으로
                    전달하므로, 이 상태로는 값이 엉뚱한 컴포넌트에 들어간다(타입이 다르면 런타임 예외, 같으면 조용히 뒤바뀜).
                    선언 순서를 %s로 바꾸고, 정적 팩토리의 new 호출 인자 순서도 함께 맞춘다.""",
                    recordType.getName(), alphabeticalOrder)
                .containsExactlyElementsOf(alphabeticalOrder);
        }
    }

    private List<Class<?>> findEmbeddedRecordTypes() {
        List<Class<?>> embeddedRecords = new ArrayList<>();

        for (Class<?> entityType : scanEntityTypes()) {
            for (Field field : entityType.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Embedded.class)) {
                    continue;
                }
                Class<?> fieldType = field.getType();
                if (fieldType.isRecord() && !embeddedRecords.contains(fieldType)) {
                    embeddedRecords.add(fieldType);
                }
            }
        }

        return embeddedRecords;
    }

    private List<Class<?>> scanEntityTypes() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        List<Class<?>> entityTypes = new ArrayList<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(ENTITY_BASE_PACKAGE)) {
            String className = candidate.getBeanClassName();
            if (className == null) {
                continue;
            }
            entityTypes.add(ClassUtils.resolveClassName(className, getClass().getClassLoader()));
        }

        return entityTypes;
    }
}
