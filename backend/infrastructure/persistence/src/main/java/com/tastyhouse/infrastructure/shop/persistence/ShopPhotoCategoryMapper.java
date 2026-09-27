package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategoryState;

final class ShopPhotoCategoryMapper {
    private ShopPhotoCategoryMapper() {
    }

    static ShopPhotoCategoryState toState(ShopPhotoCategoryJpaEntity entity) {
        return new ShopPhotoCategoryState(
            entity.getId(),
            entity.getShopId(),
            entity.getName()
        );
    }

    static ShopPhotoCategoryJpaEntity toEntity(ShopPhotoCategoryState state) {
        return ShopPhotoCategoryJpaEntity.create(
            state.shopId(),
            state.name()
        );
    }

    static void applyChanges(ShopPhotoCategoryJpaEntity entity, ShopPhotoCategoryState state) {
        entity.applyChanges(state.name());
    }
}
