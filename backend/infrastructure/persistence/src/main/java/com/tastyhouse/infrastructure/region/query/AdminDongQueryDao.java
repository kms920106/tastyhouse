package com.tastyhouse.infrastructure.region.query;

import com.tastyhouse.application.region.port.out.AdminDongQueryPort;
import com.tastyhouse.application.region.port.out.AdminDongBoundaryResult;
import com.tastyhouse.application.region.port.out.AdminDongCandidateResult;
import com.tastyhouse.application.region.port.out.AdminDongItemResult;
import com.tastyhouse.application.region.port.out.AdminDongTreeItemResult;
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

@Repository
public class AdminDongQueryDao implements AdminDongQueryPort {
    private final JPAQueryFactory queryFactory;

    public AdminDongQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    private BooleanExpression regionNameContains(String keyword) {
        return StringUtils.hasText(keyword) ? regionName().containsIgnoreCase(keyword.trim()) : null;
    }

    private StringExpression regionName() {
        return adminDongJpaEntity.sidoName
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.sigunguName)
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.dongName);
    }
}
