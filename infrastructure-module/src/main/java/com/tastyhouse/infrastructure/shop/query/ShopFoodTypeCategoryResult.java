package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shop.domain.model.FoodType;

public record ShopFoodTypeCategoryResult(
    Long id,
    FoodType foodType,
    String displayName,
    String activeIconUrl,
    String inactiveIconUrl,
    Integer sort,
    boolean visible
) {
}
