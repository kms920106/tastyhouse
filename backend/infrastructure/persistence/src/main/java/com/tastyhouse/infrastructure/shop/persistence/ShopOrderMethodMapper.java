package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopOrderMethodState;

final class ShopOrderMethodMapper {
    private ShopOrderMethodMapper() {
    }

    static ShopOrderMethodState toState(ShopOrderMethodJpaEntity entity) {
        return new ShopOrderMethodState(
            entity.getId(),
            entity.getShopId(),
            entity.getOrderMethod()
        );
    }

    static ShopOrderMethodJpaEntity toEntity(ShopOrderMethodState state) {
        return ShopOrderMethodJpaEntity.create(
            state.shopId(),
            state.orderMethod()
        );
    }
}
