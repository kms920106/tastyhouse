package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shop.model.Amenity;

public record ShopAmenityWithCategoryResult(
    Amenity amenity,
    String displayName,
    String activeIconUrl
) {
}
