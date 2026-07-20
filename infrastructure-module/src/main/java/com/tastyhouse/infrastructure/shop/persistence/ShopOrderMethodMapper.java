package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;

final class ShopOrderMethodMapper {

    private ShopOrderMethodMapper() {
    }

    static ShopOrderMethod toDomain(ShopOrderMethodJpaEntity entity) {
        return ShopOrderMethod.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getOrderMethod()
        );
    }

    static ShopOrderMethodJpaEntity toEntity(ShopOrderMethod domain) {
        return ShopOrderMethodJpaEntity.create(
            domain.getShopId(),
            domain.getOrderMethod()
        );
    }
}
