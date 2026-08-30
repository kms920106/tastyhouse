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

/**
 * {@code @Embedded} 대상 record VO의 컴포넌트 선언 순서가 이름 알파벳 오름차순인지 검증하는 가드 테스트.
 *
 * <p><b>왜 필요한가</b>: Hibernate 6의 {@code Component#sortProperties()}는 embeddable 프로퍼티를
 * 이름순으로 정렬하고, {@code isSimpleRecord()}(정렬 결과가 record 컴포넌트 순서와 일치)일 때만 정렬을
 * 건너뛴다. 선언 순서가 알파벳순이 아니면 {@code ComponentType#deepCopy}가 <b>정렬된 순서</b>로 읽은
 * 값 배열을 record canonical 생성자에 <b>선언 순서</b>대로 위치 기반 전달하므로 값이 엉뚱한 파라미터로
 * 들어간다. 결과는 둘 중 하나다.
 *
 * <ul>
 *   <li>타입이 다르면 런타임 예외 — {@code Could not instantiate entity ... argument type mismatch}
 *       (실제 장애 선례: 주문 생성 시 {@code OrderDeliveryDestination}의 {@code lotAddress}(String)가
 *       {@code distanceMeters}(Integer) 자리에 들어가 {@code Cannot cast java.lang.String to
 *       java.lang.Integer}로 500 발생)</li>
 *   <li><b>타입이 같으면 예외 없이 값만 조용히 뒤바뀐다</b> — 도로명↔지번 주소가 서로 바뀌어 저장되는 식으로,
 *       테스트가 없으면 발견되지 않는다. 이 조용한 실패 때문에 사람 눈이 아니라 이 가드가 필요하다.</li>
 * </ul>
 *
 * <p>{@code @AttributeOverride}의 {@code name}은 컴포넌트명으로 매칭되므로 선언 순서를 바꿔도 컬럼 매핑은
 * 영향받지 않는다. 즉 알파벳순 정렬은 DDL·컬럼 계약을 건드리지 않고 안전하게 지킬 수 있는 규약이다.
 *
 * <p>새 embeddable record를 추가하거나 기존 record에 컴포넌트를 끼워 넣으면 이 테스트가 자동으로 대상에
 * 포함한다(엔티티 패키지를 스캔하므로 목록을 수동 관리하지 않는다).
 */
class EmbeddedRecordComponentOrderTest {

    private static final String ENTITY_BASE_PACKAGE = "com.tastyhouse.infrastructure";

    @Test
    @DisplayName("@Embedded record VO의 컴포넌트 선언 순서는 이름 알파벳 오름차순이어야 한다")
    void embeddedRecordComponentsShouldBeInAlphabeticalOrder() {
        List<Class<?>> embeddedRecords = findEmbeddedRecordTypes();

        // 스캔이 아무것도 못 찾으면 규칙이 공허하게 통과하므로, 대상이 존재하는 것 자체를 먼저 검증한다.
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

    /**
     * 엔티티들이 {@code @Embedded}로 보유한 필드 중 record 타입만 중복 없이 수집한다.
     *
     * <p>같은 VO가 여러 엔티티에 embed될 수 있으므로(예: {@code PhoneNumber}) 타입 단위로 중복을 제거한다.
     */
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

    /** {@code @Entity} 클래스를 클래스패스 스캔으로 수집한다(엔티티 목록을 수동 관리하지 않기 위함). */
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
