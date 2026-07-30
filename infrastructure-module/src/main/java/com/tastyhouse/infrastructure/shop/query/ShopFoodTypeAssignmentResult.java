package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.core.domain.shop.domain.model.FoodType;

public record ShopFoodTypeAssignmentResult(
    Long id,
    Long foodTypeCategoryId,
    FoodType foodType,
    String displayName,
    String activeFilePath
) {
}
