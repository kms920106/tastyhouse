package com.tastyhouse.infrastructure.persistence.review.query;

import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.ShopReviewDisplaySettingOwnerQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewDisplaySettingQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewSortTypeResult;

import static com.tastyhouse.infrastructure.persistence.review.persistence.QShopReviewDisplaySettingJpaEntity.shopReviewDisplaySettingJpaEntity;

@Repository
public class ShopReviewDisplaySettingQueryAdapter implements ShopReviewDisplaySettingQueryPort, ShopReviewDisplaySettingOwnerQueryPort {

    private final JPAQueryFactory queryFactory;

    public ShopReviewDisplaySettingQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<String> findSortTypeByShopId(Long shopId) {
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
