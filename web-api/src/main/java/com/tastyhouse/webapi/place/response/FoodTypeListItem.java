package com.tastyhouse.webapi.place.response;

public record FoodTypeListItem(
    String code,
    String name,
    String activeImageUrl,
    String inactiveImageUrl
) {
    public static FoodTypeListItem from(
        String code,
        String name,
        String activeImageUrl,
        String inactiveImageUrl
    ) {
        return new FoodTypeListItem(
            code,
            name,
            activeImageUrl,
            inactiveImageUrl
        );
    }
}
