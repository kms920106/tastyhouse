package com.tastyhouse.infrastructure.persistence.shop.query;

import java.util.Optional;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopOrderNoticeJpaEntity.shopOrderNoticeJpaEntity;

@Repository
class ShopOrderNoticeQueryAdapter implements ShopOrderNoticeQueryPort, ShopOrderNoticeManagementQueryPort {

    private final JPAQueryFactory queryFactory;

    public ShopOrderNoticeQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<ShopOrderNoticeResult> findOrderNotice(Long shopId) {
        ShopOrderNoticeResult result = queryFactory
            .select(projection())
            .from(shopOrderNoticeJpaEntity)
            .where(shopOrderNoticeJpaEntity.shopId.eq(shopId))
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ShopOrderNoticeResult> findVisibleOrderNotice(Long shopId) {
        ShopOrderNoticeResult result = queryFactory
            .select(projection())
            .from(shopOrderNoticeJpaEntity)
            .where(
                shopOrderNoticeJpaEntity.shopId.eq(shopId),
                shopOrderNoticeJpaEntity.hidden.isFalse()
            )
            .fetchFirst();
        return Optional.ofNullable(result);
    }

    private ConstructorExpression<ShopOrderNoticeResult> projection() {
        return Projections.constructor(ShopOrderNoticeResult.class,
            shopOrderNoticeJpaEntity.id,
            shopOrderNoticeJpaEntity.shopId,
            shopOrderNoticeJpaEntity.content,
            shopOrderNoticeJpaEntity.hidden,
            shopOrderNoticeJpaEntity.hiddenReason
        );
    }
}
