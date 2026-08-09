package com.tastyhouse.infrastructure.region.query;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.region.persistence.QAdminDongJpaEntity.adminDongJpaEntity;

/**
 * 행정동 마스터 read 어댑터(CQRS query 측).
 *
 * <p>배달가능지역·지역별 배달팁 설정 화면에서 행정동을 고르려면 검색이 필요한데, {@code AdminDongRepository}는
 * write 포트(존재검증·주소 매칭)라 표현 목적 목록 조회를 담지 않는다. 그 조회를 이 DAO가 담당한다.
 *
 * <p>{@code regionName} 조립은 {@code ShopDeliveryAreaQueryDao}와 동일하게 SQL concat으로 완성해,
 * 프론트가 시/도·시군구·동 세 조각을 받아 문자열을 조립하지 않도록 한다.
 */
@Repository
public class AdminDongQueryDao {

    private final JPAQueryFactory queryFactory;

    public AdminDongQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 사용 중({@code is_active = true})인 행정동을 키워드로 검색한다 — 키워드가 비어 있으면 전체.
     * 주소 인덱스({@code idx_admin_dong_name}) 순서에 맞춰 시/도 → 시군구 → 동 순으로 정렬한다.
     */
    public PageResult<AdminDongItemResult> findAdminDongPage(String keyword, PageQuery pageQuery) {
        Long total = queryFactory
            .select(adminDongJpaEntity.count())
            .from(adminDongJpaEntity)
            .where(adminDongJpaEntity.active.isTrue(), regionNameContains(keyword))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<AdminDongItemResult> content = queryFactory
            .select(Projections.constructor(AdminDongItemResult.class,
                adminDongJpaEntity.id,
                adminDongJpaEntity.code,
                regionName()
            ))
            .from(adminDongJpaEntity)
            .where(adminDongJpaEntity.active.isTrue(), regionNameContains(keyword))
            .orderBy(
                adminDongJpaEntity.sidoName.asc(),
                adminDongJpaEntity.sigunguName.asc(),
                adminDongJpaEntity.dongName.asc()
            )
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 시도 목록. 하위 행정동 개수를 함께 세어 화면이 "행정동 N개"를 표시할 수 있게 한다.
     *
     * <p>전 계층을 한 번에 내리지 않고 3단 lazy 조회로 나눈 이유는, 전국 행정동이 3,600건을 넘어 한 번에
     * 내리면 응답이 비대해지고 대부분이 화면에 쓰이지 않기 때문이다.
     */
    public List<AdminDongTreeItemResult> findSidoNames() {
        return queryFactory
            .select(Projections.constructor(AdminDongTreeItemResult.class,
                adminDongJpaEntity.sidoName,
                Expressions.nullExpression(Long.class),
                Expressions.nullExpression(String.class),
                adminDongJpaEntity.count()
            ))
            .from(adminDongJpaEntity)
            .where(adminDongJpaEntity.active.isTrue())
            .groupBy(adminDongJpaEntity.sidoName)
            .orderBy(adminDongJpaEntity.sidoName.asc())
            .fetch();
    }

    /** 특정 시도의 시군구 목록. */
    public List<AdminDongTreeItemResult> findSigunguNames(String sidoName) {
        return queryFactory
            .select(Projections.constructor(AdminDongTreeItemResult.class,
                adminDongJpaEntity.sigunguName,
                Expressions.nullExpression(Long.class),
                Expressions.nullExpression(String.class),
                adminDongJpaEntity.count()
            ))
            .from(adminDongJpaEntity)
            .where(adminDongJpaEntity.active.isTrue(), adminDongJpaEntity.sidoName.eq(sidoName))
            .groupBy(adminDongJpaEntity.sigunguName)
            .orderBy(adminDongJpaEntity.sigunguName.asc())
            .fetch();
    }

    /**
     * 특정 시군구의 행정동 목록. 이 레벨에서만 식별자({@code adminDongId}·{@code code})가 채워진다 —
     * 상위 두 레벨은 그룹핑 이름일 뿐 마스터 테이블에 자기 행이 없다.
     */
    public List<AdminDongTreeItemResult> findDongs(String sidoName, String sigunguName) {
        return queryFactory
            .select(Projections.constructor(AdminDongTreeItemResult.class,
                adminDongJpaEntity.dongName,
                adminDongJpaEntity.id,
                adminDongJpaEntity.code,
                Expressions.asNumber(1L)
            ))
            .from(adminDongJpaEntity)
            .where(
                adminDongJpaEntity.active.isTrue(),
                adminDongJpaEntity.sidoName.eq(sidoName),
                adminDongJpaEntity.sigunguName.eq(sigunguName)
            )
            .orderBy(adminDongJpaEntity.dongName.asc())
            .fetch();
    }

    /**
     * 대표점이 바운딩 박스 안에 드는 행정동의 경계를 조회한다(지도 렌더링용).
     *
     * <p>경계를 보유하지 않은 동도 함께 내려보낸다 — 경계가 없다고 목록에서 빼면 화면이 "이 지역에 동이
     * 없다"로 오해하게 되고, 실제로는 좌표만 있고 경계 시드가 아직 안 들어온 정상 상태다.
     */
    public List<AdminDongBoundaryResult> findBoundariesWithinBoundingBox(
        BigDecimal minLatitude,
        BigDecimal maxLatitude,
        BigDecimal minLongitude,
        BigDecimal maxLongitude,
        int limit
    ) {
        return queryFactory
            .select(boundaryProjection())
            .from(adminDongJpaEntity)
            .where(
                adminDongJpaEntity.active.isTrue(),
                adminDongJpaEntity.centerLatitude.between(minLatitude, maxLatitude),
                adminDongJpaEntity.centerLongitude.between(minLongitude, maxLongitude)
            )
            .orderBy(adminDongJpaEntity.id.asc())
            .limit(limit)
            .fetch();
    }

    /** 식별자 목록으로 행정동 경계를 조회한다(화면이 특정 동만 다시 그릴 때). */
    public List<AdminDongBoundaryResult> findBoundariesByIds(Collection<Long> adminDongIds) {
        if (adminDongIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(boundaryProjection())
            .from(adminDongJpaEntity)
            .where(adminDongJpaEntity.active.isTrue(), adminDongJpaEntity.id.in(adminDongIds))
            .orderBy(adminDongJpaEntity.id.asc())
            .fetch();
    }

    /**
     * 좌표 판정 후보 행정동을 바운딩 박스로 좁혀 읽는다(반경·도형 미리보기용).
     *
     * <p>표시용 이름까지 조립해 내려보내므로 조회 측이 이름을 얻으려고 다시 조회하지 않는다.
     */
    public List<AdminDongCandidateResult> findCandidatesWithinBoundingBox(
        BigDecimal minLatitude,
        BigDecimal maxLatitude,
        BigDecimal minLongitude,
        BigDecimal maxLongitude
    ) {
        return queryFactory
            .select(Projections.constructor(AdminDongCandidateResult.class,
                adminDongJpaEntity.id,
                regionName(),
                adminDongJpaEntity.centerLatitude,
                adminDongJpaEntity.centerLongitude,
                adminDongJpaEntity.boundary
            ))
            .from(adminDongJpaEntity)
            .where(
                adminDongJpaEntity.active.isTrue(),
                adminDongJpaEntity.centerLatitude.between(minLatitude, maxLatitude),
                adminDongJpaEntity.centerLongitude.between(minLongitude, maxLongitude)
            )
            .orderBy(adminDongJpaEntity.id.asc())
            .fetch();
    }

    private ConstructorExpression<AdminDongBoundaryResult> boundaryProjection() {
        return Projections.constructor(AdminDongBoundaryResult.class,
            adminDongJpaEntity.id,
            regionName(),
            adminDongJpaEntity.centerLatitude,
            adminDongJpaEntity.centerLongitude,
            adminDongJpaEntity.boundary
        );
    }

    /**
     * 조립된 전체 이름에 대한 부분 일치. {@code "강남구 역삼"}처럼 시군구와 동을 이어서 입력해도 걸리도록
     * 세 컬럼을 각각 비교하지 않고 조립된 문자열 하나로 검색한다.
     */
    private BooleanExpression regionNameContains(String keyword) {
        return StringUtils.hasText(keyword) ? regionName().containsIgnoreCase(keyword.trim()) : null;
    }

    /**
     * 표시용 행정동 전체 이름({@code "서울특별시 강남구 역삼1동"})을 SQL에서 조립한다. 세 컬럼이 전부
     * NOT NULL이라 구분자 처리 분기가 없어 단순 {@code concat} 체인으로 충분하다.
     */
    private StringExpression regionName() {
        return adminDongJpaEntity.sidoName
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.sigunguName)
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.dongName);
    }
}
