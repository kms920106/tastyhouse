package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shop.model.Amenity;

public record ShopAmenityAssignmentResult(
    Long id,
    Long amenityCategoryId,
    Amenity amenity,
    String displayName,
    String activeIconUrl
) {
}
