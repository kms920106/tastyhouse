package com.tastyhouse.core.domain.shop.application.dto.command;

import com.tastyhouse.core.domain.shop.domain.model.FoodType;

public record ShopFoodTypeCategorySaveCommand(
    FoodType foodType,
    String displayName,
    Long activeImageFileId,
    Long inactiveImageFileId,
    Integer sort,
    boolean visible
) {

    public static ShopFoodTypeCategorySaveCommand of(
        FoodType foodType,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopFoodTypeCategorySaveCommand(
            foodType,
            displayName,
            activeImageFileId,
            inactiveImageFileId,
            sort,
            visible
        );
    }
}
