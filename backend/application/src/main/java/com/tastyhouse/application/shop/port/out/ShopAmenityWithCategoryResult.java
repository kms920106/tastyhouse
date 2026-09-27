package com.tastyhouse.application.shop.port.out;

public record ShopAmenityWithCategoryResult(
    String amenity,
    String displayName,
    String activeIconUrl
) {
}
