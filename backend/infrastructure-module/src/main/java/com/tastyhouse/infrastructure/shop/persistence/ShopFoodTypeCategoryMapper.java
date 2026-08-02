package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;

final class ShopFoodTypeCategoryMapper {

    private ShopFoodTypeCategoryMapper() {
    }

    static ShopFoodTypeCategory toDomain(ShopFoodTypeCategoryJpaEntity entity) {
        return ShopFoodTypeCategory.reconstitute(
            entity.getId(),
            entity.getFoodType(),
            entity.getDisplayName(),
            entity.getActiveImageFileId(),
            entity.getInactiveImageFileId(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopFoodTypeCategoryJpaEntity toEntity(ShopFoodTypeCategory domain) {
        return ShopFoodTypeCategoryJpaEntity.create(
            domain.getFoodType(),
            domain.getDisplayName(),
            domain.getActiveImageFileId(),
            domain.getInactiveImageFileId(),
            domain.getSort(),
            domain.isVisible()
        );
    }

    static void applyChanges(ShopFoodTypeCategoryJpaEntity entity, ShopFoodTypeCategory domain) {
        entity.applyChanges(
            domain.getDisplayName(),
            domain.getActiveImageFileId(),
            domain.getInactiveImageFileId(),
            domain.getSort(),
            domain.isVisible()
        );
    }
}
