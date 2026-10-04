package com.tastyhouse.infrastructure.persistence.menureview.query;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.menureview.port.out.MenuReviewMemberCountResult;
import com.tastyhouse.application.menureview.port.out.MenuReviewStatisticsQueryPort;

import static com.tastyhouse.infrastructure.persistence.menureview.persistence.QMenuReviewJpaEntity.menuReviewJpaEntity;

@Repository
public class MenuReviewStatisticsQueryAdapter implements MenuReviewStatisticsQueryPort {

    private final JPAQueryFactory queryFactory;

    public MenuReviewStatisticsQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Long countVisibleByProductId(Long productId) {
        return queryFactory
            .select(menuReviewJpaEntity.count())
            .from(menuReviewJpaEntity)
            .where(menuReviewJpaEntity.productId.eq(productId), menuReviewJpaEntity.hidden.isFalse())
            .fetchOne();
    }

    @Override
    public Double getAverageRatingByProductId(Long productId) {
        return queryFactory
            .select(menuReviewJpaEntity.rating.avg())
            .from(menuReviewJpaEntity)
            .where(menuReviewJpaEntity.productId.eq(productId), menuReviewJpaEntity.hidden.isFalse())
            .fetchOne();
    }

    @Override
    public List<MenuReviewMemberCountResult> countByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        NumberPath<Long> memberIdPath = menuReviewJpaEntity.memberId;

        return queryFactory
            .select(Projections.constructor(MenuReviewMemberCountResult.class,
                memberIdPath,
                menuReviewJpaEntity.count(),
                menuReviewJpaEntity.createdAt.max()
            ))
            .from(menuReviewJpaEntity)
            .where(
                menuReviewJpaEntity.createdAt.goe(startDate),
                menuReviewJpaEntity.createdAt.lt(endDate),
                menuReviewJpaEntity.hidden.isFalse()
            )
            .groupBy(memberIdPath)
            .fetch();
    }
}
