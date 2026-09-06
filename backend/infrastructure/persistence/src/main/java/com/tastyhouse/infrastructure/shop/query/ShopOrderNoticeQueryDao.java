package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;
import java.util.Optional;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.shop.persistence.QShopOrderNoticeJpaEntity.shopOrderNoticeJpaEntity;

@Repository
public class ShopOrderNoticeQueryDao implements ShopOrderNoticeQueryPort, ShopOrderNoticeManagementQueryPort {
    private final JPAQueryFactory queryFactory;

    public ShopOrderNoticeQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<ShopOrderNoticeResult> findOrderNotice(Long shopId) {
        return Optional.ofNullable(queryFactory
            .select(projection())
            .from(shopOrderNoticeJpaEntity)
            .where(shopOrderNoticeJpaEntity.shopId.eq(shopId))
            .fetchFirst());
    }

    @Override
    public Optional<ShopOrderNoticeResult> findVisibleOrderNotice(Long shopId) {
        return Optional.ofNullable(queryFactory
            .select(projection())
            .from(shopOrderNoticeJpaEntity)
            .where(
                shopOrderNoticeJpaEntity.shopId.eq(shopId),
                shopOrderNoticeJpaEntity.hidden.isFalse()
            )
            .fetchFirst());
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
