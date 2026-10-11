package com.tastyhouse.application.shop.port.out;

public record ShopFoodTypeCategoryResult(
    Long id,
    String foodType,
    String displayName,
    String activeIconUrl,
    String inactiveIconUrl,
    Integer sort,
    boolean visible
) {
}
