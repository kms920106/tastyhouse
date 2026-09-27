package com.tastyhouse.application.shop.port.out.write;

public record ShopFoodTypeState(
    Long id,
    Long shopId,
    Long shopFoodTypeCategoryId
) {
}
