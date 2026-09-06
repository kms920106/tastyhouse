package com.tastyhouse.infrastructure.review.query;

import com.tastyhouse.application.review.port.out.ShopReviewDisplaySettingOwnerQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewDisplaySettingQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewSortTypeResult;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewSortType;

import static com.tastyhouse.infrastructure.review.persistence.QShopReviewDisplaySettingJpaEntity.shopReviewDisplaySettingJpaEntity;

@Repository
public class ShopReviewDisplaySettingQueryDao implements ShopReviewDisplaySettingQueryPort, ShopReviewDisplaySettingOwnerQueryPort {
    private final JPAQueryFactory queryFactory;

    public ShopReviewDisplaySettingQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<ReviewSortType> findSortTypeByShopId(Long shopId) {
        return Optional.ofNullable(queryFactory
            .select(shopReviewDisplaySettingJpaEntity.sortType)
            .from(shopReviewDisplaySettingJpaEntity)
            .where(shopReviewDisplaySettingJpaEntity.shopId.eq(shopId))
            .fetchOne());
    }

    @Override
    public Optional<ShopReviewSortTypeResult> findSortTypeSettingByShopId(Long shopId) {
        return Optional.ofNullable(queryFactory
            .select(Projections.constructor(ShopReviewSortTypeResult.class,
                shopReviewDisplaySettingJpaEntity.sortType,
                shopReviewDisplaySettingJpaEntity.updatedAt
            ))
            .from(shopReviewDisplaySettingJpaEntity)
            .where(shopReviewDisplaySettingJpaEntity.shopId.eq(shopId))
            .fetchOne());
    }
}
