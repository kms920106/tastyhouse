package com.tastyhouse.webapi.place.response;

public record AmenityListItem(
    String code,
    String name,
    String activeImageUrl,
    String inactiveImageUrl
) {
    public static AmenityListItem from(
    String code,
    String name,
    String activeImageUrl,
    String inactiveImageUrl
    ) {
    return new AmenityListItem(
        code,
        name,
        activeImageUrl,
        inactiveImageUrl
    );
    }
}
