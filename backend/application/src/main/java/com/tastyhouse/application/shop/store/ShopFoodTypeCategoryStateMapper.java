package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.FoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.application.shop.port.out.write.ShopFoodTypeCategoryState;

final class ShopFoodTypeCategoryStateMapper {
    private ShopFoodTypeCategoryStateMapper() {
    }

    static ShopFoodTypeCategory toDomain(ShopFoodTypeCategoryState state) {
        return ShopFoodTypeCategory.reconstitute(
            state.id(),
            state.foodType() == null ? null : FoodType.valueOf(state.foodType()),
            state.displayName(),
            state.activeImageFileId() == null ? null : UploadedFileId.of(state.activeImageFileId()),
            state.inactiveImageFileId() == null ? null : UploadedFileId.of(state.inactiveImageFileId()),
            state.sort(),
            state.visible()
        );
    }

    static ShopFoodTypeCategoryState toState(ShopFoodTypeCategory shopFoodTypeCategory) {
        return new ShopFoodTypeCategoryState(
            shopFoodTypeCategory.getId(),
            shopFoodTypeCategory.getFoodType() == null ? null : shopFoodTypeCategory.getFoodType().name(),
            shopFoodTypeCategory.getDisplayName(),
            shopFoodTypeCategory.getActiveImageFileId() == null ? null : shopFoodTypeCategory.getActiveImageFileId().value(),
            shopFoodTypeCategory.getInactiveImageFileId() == null ? null : shopFoodTypeCategory.getInactiveImageFileId().value(),
            shopFoodTypeCategory.getSort(),
            shopFoodTypeCategory.isVisible()
        );
    }
}
