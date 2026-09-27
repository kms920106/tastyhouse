package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopOrderMethodMapper {
    private ShopOrderMethodMapper() {
    }

    static ShopOrderMethod toDomain(ShopOrderMethodJpaEntity entity) {
        return ShopOrderMethod.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getOrderMethod() == null ? null : OrderMethod.valueOf(entity.getOrderMethod())
        );
    }

    static ShopOrderMethodJpaEntity toEntity(ShopOrderMethod shopOrderMethod) {
        return ShopOrderMethodJpaEntity.create(
            shopOrderMethod.getShopId() == null ? null : shopOrderMethod.getShopId().value(),
            shopOrderMethod.getOrderMethod() == null ? null : shopOrderMethod.getOrderMethod().name()
        );
    }
}
