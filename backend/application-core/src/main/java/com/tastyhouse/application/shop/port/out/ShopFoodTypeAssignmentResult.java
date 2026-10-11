package com.tastyhouse.application.shop.port.out;

public record ShopFoodTypeAssignmentResult(
    Long id,
    Long foodTypeCategoryId,
    String foodType,
    String displayName,
    String activeIconUrl
) {
}
