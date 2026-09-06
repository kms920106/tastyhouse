package com.tastyhouse.infrastructure.menureview.query;

import com.tastyhouse.application.menureview.port.out.MenuReviewStatisticsQueryPort;
import com.tastyhouse.application.menureview.port.out.MenuReviewMemberCountResult;
import com.querydsl.core.types.Projections;
import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.menureview.persistence.QMenuReviewJpaEntity.menuReviewJpaEntity;

@Repository
public class MenuReviewStatisticsQueryDao implements MenuReviewStatisticsQueryPort {
    private final JPAQueryFactory queryFactory;

    public MenuReviewStatisticsQueryDao(JPAQueryFactory queryFactory) {
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
