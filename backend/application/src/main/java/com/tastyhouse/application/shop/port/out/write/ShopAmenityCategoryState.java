package com.tastyhouse.application.shop.port.out.write;

public record ShopAmenityCategoryState(
    Long id,
    String amenity,
    String displayName,
    Long activeImageFileId,
    Long inactiveImageFileId,
    Integer sort,
    boolean visible
) {
}
