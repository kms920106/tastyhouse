package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.application.shop.port.out.write.ShopOrderMethodLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopOrderMethodSavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopOrderMethodJpaEntity.shopOrderMethodJpaEntity;

@Repository
class ShopOrderMethodPersistenceAdapter implements ShopOrderMethodLoadPort, ShopOrderMethodSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopOrderMethodJpaRepository shopOrderMethodJpaRepository;

    public ShopOrderMethodPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopOrderMethodJpaRepository shopOrderMethodJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.shopOrderMethodJpaRepository = shopOrderMethodJpaRepository;
    }

    @Override
    public List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopOrderMethodJpaEntity)
            .where(shopOrderMethodJpaEntity.shopId.eq(shopId))
            .orderBy(shopOrderMethodJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(ShopOrderMethodMapper::toDomain)
            .toList();
    }

    @Override
    public ShopOrderMethod saveOrderMethod(ShopOrderMethod orderMethod) {
        if (orderMethod.getId() == null) {
            ShopOrderMethodJpaEntity saved = shopOrderMethodJpaRepository.save(ShopOrderMethodMapper.toEntity(orderMethod));
            return ShopOrderMethodMapper.toDomain(saved);
        }

        ShopOrderMethodJpaEntity entity = shopOrderMethodJpaRepository.findById(orderMethod.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문방식 배정입니다: " + orderMethod.getId()));
        return ShopOrderMethodMapper.toDomain(entity);
    }

    @Override
    public void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod) {
        List<ShopOrderMethodJpaEntity> rows = queryFactory
            .selectFrom(shopOrderMethodJpaEntity)
            .where(
                shopOrderMethodJpaEntity.shopId.eq(shopId),
                orderMethodEq(orderMethod)
            )
            .fetch();
        shopOrderMethodJpaRepository.deleteAll(rows);
    }

    private BooleanExpression orderMethodEq(OrderMethod orderMethod) {
        return orderMethod == null
            ? shopOrderMethodJpaEntity.orderMethod.isNull()
            : shopOrderMethodJpaEntity.orderMethod.eq(orderMethod.name());
    }
}
