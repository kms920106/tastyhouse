package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.core.domain.shop.domain.model.Amenity;

public record ShopAmenityWithCategoryResult(
    Amenity amenity,
    String displayName,
    String activeFilePath
) {
}
