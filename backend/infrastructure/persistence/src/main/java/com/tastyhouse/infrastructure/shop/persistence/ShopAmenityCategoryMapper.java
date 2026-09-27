package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopAmenityCategoryState;

final class ShopAmenityCategoryMapper {
    private ShopAmenityCategoryMapper() {
    }

    static ShopAmenityCategoryState toState(ShopAmenityCategoryJpaEntity entity) {
        return new ShopAmenityCategoryState(
            entity.getId(),
            entity.getAmenity(),
            entity.getDisplayName(),
            entity.getActiveImageFileId(),
            entity.getInactiveImageFileId(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopAmenityCategoryJpaEntity toEntity(ShopAmenityCategoryState state) {
        return ShopAmenityCategoryJpaEntity.create(
            state.amenity(),
            state.displayName(),
            state.activeImageFileId(),
            state.inactiveImageFileId(),
            state.sort(),
            state.visible()
        );
    }

    static void applyChanges(ShopAmenityCategoryJpaEntity entity, ShopAmenityCategoryState state) {
        entity.applyChanges(
            state.displayName(),
            state.activeImageFileId(),
            state.inactiveImageFileId(),
            state.sort(),
            state.visible()
        );
    }
}
