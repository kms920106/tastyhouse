package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.FoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;

final class ShopFoodTypeCategoryMapper {
    private ShopFoodTypeCategoryMapper() {
    }

    static ShopFoodTypeCategory toDomain(ShopFoodTypeCategoryJpaEntity entity) {
        return ShopFoodTypeCategory.reconstitute(
            entity.getId(),
            entity.getFoodType() == null ? null : FoodType.valueOf(entity.getFoodType()),
            entity.getDisplayName(),
            entity.getActiveImageFileId() == null ? null : UploadedFileId.of(entity.getActiveImageFileId()),
            entity.getInactiveImageFileId() == null ? null : UploadedFileId.of(entity.getInactiveImageFileId()),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopFoodTypeCategoryJpaEntity toEntity(ShopFoodTypeCategory shopFoodTypeCategory) {
        return ShopFoodTypeCategoryJpaEntity.create(
            shopFoodTypeCategory.getFoodType() == null ? null : shopFoodTypeCategory.getFoodType().name(),
            shopFoodTypeCategory.getDisplayName(),
            shopFoodTypeCategory.getActiveImageFileId() == null ? null : shopFoodTypeCategory.getActiveImageFileId().value(),
            shopFoodTypeCategory.getInactiveImageFileId() == null ? null : shopFoodTypeCategory.getInactiveImageFileId().value(),
            shopFoodTypeCategory.getSort(),
            shopFoodTypeCategory.isVisible()
        );
    }

    static void applyChanges(ShopFoodTypeCategoryJpaEntity entity, ShopFoodTypeCategory shopFoodTypeCategory) {
        entity.applyChanges(
            shopFoodTypeCategory.getDisplayName(),
            shopFoodTypeCategory.getActiveImageFileId() == null ? null : shopFoodTypeCategory.getActiveImageFileId().value(),
            shopFoodTypeCategory.getInactiveImageFileId() == null ? null : shopFoodTypeCategory.getInactiveImageFileId().value(),
            shopFoodTypeCategory.getSort(),
            shopFoodTypeCategory.isVisible()
        );
    }
}
