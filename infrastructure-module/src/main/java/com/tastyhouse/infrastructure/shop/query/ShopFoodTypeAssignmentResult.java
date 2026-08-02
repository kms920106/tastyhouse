package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shop.domain.model.FoodType;

public record ShopFoodTypeAssignmentResult(
    Long id,
    Long foodTypeCategoryId,
    FoodType foodType,
    String displayName,
    String activeIconUrl
) {
}
