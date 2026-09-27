package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaState;

final class ShopDeliveryAreaMapper {
    private ShopDeliveryAreaMapper() {
    }

    static ShopDeliveryAreaState toState(ShopDeliveryAreaJpaEntity entity) {
        return new ShopDeliveryAreaState(
            entity.getId(),
            entity.getShopId(),
            entity.getAdminDongId(),
            entity.getSource()
        );
    }

    static ShopDeliveryAreaJpaEntity toEntity(ShopDeliveryAreaState state) {
        return ShopDeliveryAreaJpaEntity.create(
            state.shopId(),
            state.adminDongId(),
            state.source()
        );
    }
}
