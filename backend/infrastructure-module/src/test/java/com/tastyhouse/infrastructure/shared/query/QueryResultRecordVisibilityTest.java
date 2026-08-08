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

/**
 * {@code <ctx>/query/} 이하 Result record가 {@code public}으로 선언되었는지 검증하는 가드 테스트.
 *
 * <p><b>왜 필요한가</b>: {@code Projections.constructor(Xxx.class, ...)}가 만드는 QueryDSL
 * {@code ConstructorExpression}은 대상 타입의 생성자를 {@code Class#getConstructors()}로 탐색하는데,
 * 이 메서드는 <b>public 생성자만</b> 반환한다. package-private record의 canonical 생성자는
 * package-private이므로, 같은 패키지의 DAO가 호출하더라도 리플렉션 탐색에서는 보이지 않는다.
 *
 * <p>문제는 이 실패가 <b>컴파일 타임에 드러나지 않는다</b>는 점이다. {@code Projections.constructor}는
 * {@code Class<?>}를 받으므로 package-private record를 넘겨도 컴파일이 통과하고, 해당 쿼리가 실제로
 * 실행되는 순간에야 아래 예외로 500이 난다.
 *
 * <pre>
 * com.querydsl.core.types.ExpressionException: No constructor found for class
 * com.tastyhouse.infrastructure.shop.query.ShopRiderGuidePickupPresenceResult
 * with parameters: [class java.lang.Long, class java.lang.String, ...]
 * </pre>
 *
 * <p>실제 장애 선례: {@code ShopRiderGuidePickupPresenceResult}가 "DAO 내부에서만 쓰는 중간 투영이니
 * 노출을 좁힌다"는 의도로 package-private으로 선언되어, admin "라이더 안내 검수" 목록 조회
 * ({@code GET /api/shops/v1/rider-guides})가 전부 500으로 실패했다. 같은 패키지의 다른 Result record
 * 30여 개는 모두 {@code public}이라 이 한 건만 어긋난 상태였고, 빌드·리뷰 어디에서도 걸리지 않아
 * 브라우저 검증 단계에서야 발견됐다.
 *
 * <p>이 조용한 실패 때문에 사람 눈이 아니라 이 가드가 필요하다. 새 Result record를 추가하면 패키지를
 * 스캔해 자동으로 대상에 포함하므로 목록을 수동 관리하지 않는다.
 *
 * <p>루트 {@code CLAUDE.md}의 record 파일 분리 규칙("별도 파일로 분리한 record는 {@code public}으로
 * 선언한다")을 기계적으로 강제하는 역할도 겸한다.
 */
class QueryResultRecordVisibilityTest {

    private static final String QUERY_PACKAGE_PATTERN =
        "classpath*:com/tastyhouse/infrastructure/**/query/*.class";

    @Test
    @DisplayName("query 패키지의 Result record는 public이어야 한다 (QueryDSL Projections.constructor 탐색 대상)")
    void queryResultRecordsShouldBePublic() {
        List<Class<?>> resultRecords = findQueryPackageRecords();

        // 스캔이 아무것도 못 찾으면 규칙이 공허하게 통과하므로, 대상이 존재하는 것 자체를 먼저 검증한다.
        assertThat(resultRecords)
            .as("query 패키지에서 record를 하나도 찾지 못했다 — 스캔 패턴(%s)이 잘못되었을 수 있다", QUERY_PACKAGE_PATTERN)
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

    /**
     * {@code <ctx>/query/} 이하의 <b>최상위</b> record 타입을 클래스패스 스캔으로 수집한다.
     *
     * <p>{@code Projections.constructor}에 실제로 넘겨지는지를 정적으로 판별할 수는 없으므로, 그 패키지의
     * 최상위 record 전체를 대상으로 삼는다. 투영에 쓰이지 않는 record(예: {@code SearchCondition})도
     * CLAUDE.md의 record 파일 분리 규칙상 어차피 {@code public}이어야 하므로 과잉 검사가 되지 않는다.
     *
     * <p><b>중첩 record는 제외한다</b>: DAO 본문 안에 선언된 {@code private} 헬퍼 record(예:
     * {@code ProductQueryDao.BatchOptionInfo})는 투영이 아니라 {@code new}로 직접 조립하는 내부 계산용이라
     * 리플렉션 탐색 대상이 아니다. 이런 record까지 {@code public}을 강요하면 규칙의 근거(QueryDSL 생성자
     * 탐색)와 무관하게 노출만 넓히게 되므로, 검사 범위를 독립 파일로 분리된 최상위 record로 한정한다.
     * (중첩 record를 투영에 쓰려 한다면 애초에 CLAUDE.md의 record 파일 분리 규칙에 따라 독립 파일로 빼야
     * 하고, 그 시점에 이 가드의 대상이 된다.)
     */
    private List<Class<?>> findQueryPackageRecords() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);

        List<Class<?>> records = new ArrayList<>();
        try {
            for (Resource resource : resolver.getResources(QUERY_PACKAGE_PATTERN)) {
                String className = metadataReaderFactory.getMetadataReader(resource)
                    .getClassMetadata()
                    .getClassName();
                // 중첩 클래스는 바이너리명에 '$'가 들어간다 — 최상위 record만 남긴다.
                if (className.contains("$")) {
                    continue;
                }
                Class<?> type = ClassUtils.resolveClassName(className, getClass().getClassLoader());
                if (type.isRecord()) {
                    records.add(type);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("query 패키지 클래스 스캔에 실패했다: " + QUERY_PACKAGE_PATTERN, e);
        }

        return records;
    }
}
