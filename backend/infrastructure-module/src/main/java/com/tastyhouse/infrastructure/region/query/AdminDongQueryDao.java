package com.tastyhouse.infrastructure.region.query;

import java.util.List;

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
