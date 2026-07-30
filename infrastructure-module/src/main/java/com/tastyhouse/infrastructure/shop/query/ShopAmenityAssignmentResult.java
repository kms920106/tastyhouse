package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.core.domain.shop.domain.model.Amenity;

public record ShopAmenityAssignmentResult(
    Long id,
    Long amenityCategoryId,
    Amenity amenity,
    String displayName,
    String activeFilePath
) {
}
