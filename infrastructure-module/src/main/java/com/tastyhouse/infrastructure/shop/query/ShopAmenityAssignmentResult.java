package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shop.domain.model.Amenity;

public record ShopAmenityAssignmentResult(
    Long id,
    Long amenityCategoryId,
    Amenity amenity,
    String displayName,
    String activeIconUrl
) {
}
