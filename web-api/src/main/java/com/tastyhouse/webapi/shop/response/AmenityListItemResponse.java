package com.tastyhouse.webapi.shop.response;

public record AmenityListItemResponse(
    String code,
    String name,
    String activeImageUrl,
    String inactiveImageUrl
) {
    public static AmenityListItemResponse from(
        String code,
        String name,
        String activeImageUrl,
        String inactiveImageUrl
    ) {
        return new AmenityListItemResponse(
            code,
            name,
            activeImageUrl,
            inactiveImageUrl
        );
    }
}
