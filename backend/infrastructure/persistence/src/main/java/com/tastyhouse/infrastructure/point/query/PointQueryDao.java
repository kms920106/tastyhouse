package com.tastyhouse.infrastructure.point.query;

import com.tastyhouse.application.point.port.out.PointManagementQueryPort;
import com.tastyhouse.application.point.port.out.PointQueryPort;
import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryResult;
import com.tastyhouse.application.point.port.out.PointSearchCondition;
import com.querydsl.core.types.Projections;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.point.model.PointType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.point.persistence.QPointHistoryJpaEntity.pointHistoryJpaEntity;
import static com.tastyhouse.infrastructure.point.persistence.QPointJpaEntity.pointJpaEntity;

@Repository
public class PointQueryDao implements PointQueryPort, PointManagementQueryPort {
    private final JPAQueryFactory queryFactory;

    public PointQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<PointBalanceResult> findBalanceByMemberId(Long memberId) {
        PointBalanceResult balance = queryFactory
            .select(Projections.constructor(PointBalanceResult.class,
                pointJpaEntity.availablePoints,
                pointJpaEntity.expiredThisMonth
            ))
            .from(pointJpaEntity)
            .where(pointJpaEntity.memberId.eq(memberId))
            .fetchOne();

        return Optional.ofNullable(balance);
    }

    @Override
    public List<PointHistoryResult> findPointHistories(Long memberId) {
        return queryFactory
            .select(Projections.constructor(PointHistoryResult.class,
                pointHistoryJpaEntity.pointType,
                pointHistoryJpaEntity.pointAmount,
                pointHistoryJpaEntity.reason,
                pointHistoryJpaEntity.createdAt
            ))
            .from(pointHistoryJpaEntity)
            .where(pointHistoryJpaEntity.memberId.eq(memberId))
            .orderBy(pointHistoryJpaEntity.createdAt.desc())
            .fetch();
    }

    @Override
    public PageResult<PointHistoryResult> findPointHistoryPage(PointSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(pointHistoryJpaEntity.id.count())
            .from(pointHistoryJpaEntity)
            .where(
                pointHistoryJpaEntity.memberId.eq(condition.memberId()),
                pointTypeEq(condition.pointType())
            )
            .fetchOne();

        List<PointHistoryResult> content = queryFactory
            .select(Projections.constructor(PointHistoryResult.class,
                pointHistoryJpaEntity.pointType,
                pointHistoryJpaEntity.pointAmount,
                pointHistoryJpaEntity.reason,
                pointHistoryJpaEntity.createdAt
            ))
            .from(pointHistoryJpaEntity)
            .where(
                pointHistoryJpaEntity.memberId.eq(condition.memberId()),
                pointTypeEq(condition.pointType())
            )
            .orderBy(pointHistoryJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    private BooleanExpression pointTypeEq(PointType pointType) {
        return pointType != null ? pointHistoryJpaEntity.pointType.eq(pointType) : null;
    }
}
