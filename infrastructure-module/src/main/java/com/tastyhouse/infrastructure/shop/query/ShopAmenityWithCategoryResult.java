package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shop.domain.model.Amenity;

public record ShopAmenityWithCategoryResult(
    Amenity amenity,
    String displayName,
    String activeIconUrl
) {
}
