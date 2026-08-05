package com.tastyhouse.infrastructure.shared.query;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;

/**
 * {@code @Convert}로 도메인 VO에 매핑된 FK 컬럼을 raw {@code Long} 값과 비교하기 위한 헬퍼.
 *
 * <p><b>왜 필요한가</b>: query DAO 계층은 항상 raw {@code Long}을 쓰지만(ID VO 경계 규칙), 엔티티 FK가
 * {@code @Convert}로 VO에 매핑돼 있으면 Hibernate는 그 경로의 파라미터 타입을 VO로 해석한다. 따라서
 * {@code Expressions.numberPath(Long.class, ...).eq(1L)}처럼 raw 값을 바인딩하면 컴파일은 통과하지만
 * 조회 시점에 다음 예외로 500이 된다.
 *
 * <pre>
 * QueryArgumentException: Argument [1] of type [java.lang.Long]
 *   did not match parameter type [com.tastyhouse.domain.shop.vo.ShopId]
 * </pre>
 *
 * <p><b>numberPath 우회를 대체하지 않는다</b>: {@code numberPath}는 <i>경로 대 경로</i> 비교(join
 * {@code on} 절)와 투영·정렬·집계에서는 파라미터 바인딩이 없어 그대로 정상 동작하므로 계속 쓴다. 이
 * 클래스는 <b>raw 값을 바인딩하는 비교(where의 eq/in)</b>에만 쓴다 — 그 지점에서만 위 예외가 난다.
 *
 * <pre>
 * // 값 비교 — 이 헬퍼로 VO 승격 후 비교
 * .where(ConvertedIdPaths.eq(shopContentBoardJpaEntity, "shopId", ShopId.class, ShopId::of, shopId))
 *
 * // 경로 대 경로 조인 — 기존 numberPath 그대로
 * .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(contentBoardImageFileId()))
 * </pre>
 */
public final class ConvertedIdPaths {

    private ConvertedIdPaths() {
    }

    /**
     * {@code @Convert} VO 컬럼을 raw {@code Long}과 같은지 비교한다. {@code value}가 null이면
     * null을 반환해 QueryDSL varargs {@code where(...)}에서 조건이 무시되도록 한다(동적 검색 관용구).
     */
    public static <T> BooleanExpression eq(
        Path<?> entity,
        String field,
        Class<T> voType,
        Function<Long, T> promoter,
        Long value
    ) {
        return value == null ? null : path(entity, field, voType).eq(promoter.apply(value));
    }

    /**
     * {@code @Convert} VO 컬럼을 raw {@code Long} 집합에 포함되는지 비교한다. {@code values}가 null이거나
     * 비어 있으면 null을 반환한다 — 빈 {@code in ()}은 DB에 따라 문법 오류가 되므로 조건을 생략한다.
     */
    public static <T> BooleanExpression in(
        Path<?> entity,
        String field,
        Class<T> voType,
        Function<Long, T> promoter,
        Collection<Long> values
    ) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<T> promoted = values.stream().map(promoter).toList();
        return path(entity, field, voType).in(promoted);
    }

    /**
     * VO 타입 path를 만든다. VO는 {@code Comparable}이 아니므로 {@code comparablePath}가 아니라
     * {@code Expressions.path}를 쓴다(동등 비교·in만 필요하며 크기 비교는 쓰지 않는다).
     */
    private static <T> SimpleExpression<T> path(Path<?> entity, String field, Class<T> voType) {
        return Expressions.path(voType, entity, field);
    }

    /**
     * {@code @Convert} VO 컬럼을 raw {@code Long}으로 <b>투영</b>하기 위한 표현식.
     *
     * <p>비교(where)와 달리 투영에서는 반대 방향의 문제가 난다 — {@code Expressions.numberPath(Long.class,
     * ...)}로 선언해도 Hibernate는 컨버터를 적용해 <b>VO 인스턴스</b>를 돌려주므로, 그 값을 {@code Long}
     * 필드를 가진 Result record 생성자에 넘길 때 다음 예외로 500이 된다(해당 shopId의 행이 <b>존재할
     * 때만</b> 터지므로 빈 테이블에서는 조용히 통과한다 — 실제로 이 결함이 오래 숨어 있던 이유다).
     *
     * <pre>
     * InvalidDataAccessApiUsageException: argument type mismatch
     *   Caused by: java.lang.IllegalArgumentException: argument type mismatch
     * </pre>
     *
     * <p>{@code CAST(col AS Long)}로 컨버터를 우회해 DB 값을 그대로 {@code Long}으로 읽는다. SQL에는
     * {@code cast(... as signed)}가 추가되며 인덱스를 쓰는 {@code where}절이 아니라 select 목록에만
     * 나타나므로 실행 계획에 영향이 없다.
     */
    public static NumberExpression<Long> longValue(Path<?> entity, String field, Class<?> voType) {
        return Expressions.numberTemplate(Long.class, "cast({0} as long)", path(entity, field, voType));
    }
}
