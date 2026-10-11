package com.tastyhouse.application.shop.port.out;

public record ShopAmenityCategoryResult(
    Long id,
    String amenity,
    String displayName,
    String activeIconUrl,
    String inactiveIconUrl,
    Integer sort,
    boolean visible
) {
}
