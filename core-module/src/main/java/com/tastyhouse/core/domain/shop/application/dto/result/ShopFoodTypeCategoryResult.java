package com.tastyhouse.core.domain.shop.application.dto.result;

import com.tastyhouse.core.domain.shop.domain.model.FoodType;

public record ShopFoodTypeCategoryResult(
    Long id,
    FoodType foodType,
    String displayName,
    String activeFilePath,
    String inactiveFilePath,
    Integer sort,
    boolean visible
) {
}
