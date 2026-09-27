package com.tastyhouse.application.shop.port.out.write;

public record ShopFoodTypeCategoryState(
    Long id,
    String foodType,
    String displayName,
    Long activeImageFileId,
    Long inactiveImageFileId,
    Integer sort,
    boolean visible
) {
}
