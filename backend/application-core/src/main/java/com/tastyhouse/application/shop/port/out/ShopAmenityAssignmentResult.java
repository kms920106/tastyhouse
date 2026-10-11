package com.tastyhouse.application.shop.port.out;

public record ShopAmenityAssignmentResult(
    Long id,
    Long amenityCategoryId,
    String amenity,
    String displayName,
    String activeIconUrl
) {
}
