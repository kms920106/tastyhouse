package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shop.model.FoodType;

public record ShopFoodTypeAssignmentResult(
    Long id,
    Long foodTypeCategoryId,
    FoodType foodType,
    String displayName,
    String activeIconUrl
) {
}
