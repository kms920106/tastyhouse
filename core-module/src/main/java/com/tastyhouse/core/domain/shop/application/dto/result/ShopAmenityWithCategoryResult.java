package com.tastyhouse.core.domain.shop.application.dto.result;

import com.tastyhouse.core.domain.shop.domain.model.Amenity;

public record ShopAmenityWithCategoryResult(
    Amenity amenity,
    String displayName,
    String activeFilePath
) {
}
