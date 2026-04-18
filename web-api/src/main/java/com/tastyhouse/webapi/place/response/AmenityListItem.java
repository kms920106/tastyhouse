package com.tastyhouse.webapi.place.response;

public record AmenityListItem(
    String code,
    String name,
    String imageUrlOn,
    String imageUrlOff
) {
    public static AmenityListItem from(
    String code,
    String name,
    String imageUrlOn,
    String imageUrlOff
    ) {
    return new AmenityListItem(
        code,
        name,
        imageUrlOn,
        imageUrlOff
    );
    }
}
