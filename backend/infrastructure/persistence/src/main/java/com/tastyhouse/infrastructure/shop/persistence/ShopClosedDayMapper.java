package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopClosedDayState;

final class ShopClosedDayMapper {
    private ShopClosedDayMapper() {
    }

    static ShopClosedDayState toState(ShopClosedDayJpaEntity entity) {
        return new ShopClosedDayState(
            entity.getId(),
            entity.getShopId(),
            entity.getClosedDayType()
        );
    }

    static ShopClosedDayJpaEntity toEntity(ShopClosedDayState state) {
        return ShopClosedDayJpaEntity.create(
            state.shopId(),
            state.closedDayType()
        );
    }
}
