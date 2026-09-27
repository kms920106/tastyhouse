package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopFoodTypeCategoryState;

final class ShopFoodTypeCategoryMapper {
    private ShopFoodTypeCategoryMapper() {
    }

    static ShopFoodTypeCategoryState toState(ShopFoodTypeCategoryJpaEntity entity) {
        return new ShopFoodTypeCategoryState(
            entity.getId(),
            entity.getFoodType(),
            entity.getDisplayName(),
            entity.getActiveImageFileId(),
            entity.getInactiveImageFileId(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopFoodTypeCategoryJpaEntity toEntity(ShopFoodTypeCategoryState state) {
        return ShopFoodTypeCategoryJpaEntity.create(
            state.foodType(),
            state.displayName(),
            state.activeImageFileId(),
            state.inactiveImageFileId(),
            state.sort(),
            state.visible()
        );
    }

    static void applyChanges(ShopFoodTypeCategoryJpaEntity entity, ShopFoodTypeCategoryState state) {
        entity.applyChanges(
            state.displayName(),
            state.activeImageFileId(),
            state.inactiveImageFileId(),
            state.sort(),
            state.visible()
        );
    }
}
