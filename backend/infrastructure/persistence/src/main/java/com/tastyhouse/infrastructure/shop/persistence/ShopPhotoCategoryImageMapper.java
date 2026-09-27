package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategoryImageState;

final class ShopPhotoCategoryImageMapper {
    private ShopPhotoCategoryImageMapper() {
    }

    static ShopPhotoCategoryImageState toState(ShopPhotoCategoryImageJpaEntity entity) {
        return new ShopPhotoCategoryImageState(
            entity.getId(),
            entity.getShopPhotoCategoryId(),
            entity.getImageFileId(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopPhotoCategoryImageJpaEntity toEntity(ShopPhotoCategoryImageState state) {
        return ShopPhotoCategoryImageJpaEntity.create(
            state.shopPhotoCategoryId(),
            state.imageFileId(),
            state.sort(),
            state.visible()
        );
    }

    static void applyChanges(ShopPhotoCategoryImageJpaEntity entity, ShopPhotoCategoryImageState state) {
        entity.applyChanges(
            state.imageFileId(),
            state.sort(),
            state.visible()
        );
    }
}
