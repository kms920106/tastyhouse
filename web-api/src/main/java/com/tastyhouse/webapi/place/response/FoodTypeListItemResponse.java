package com.tastyhouse.webapi.place.response;

public record FoodTypeListItemResponse(
    String code,
    String name,
    String activeImageUrl,
    String inactiveImageUrl
) {
    public static FoodTypeListItemResponse from(
        String code,
        String name,
        String activeImageUrl,
        String inactiveImageUrl
    ) {
        return new FoodTypeListItemResponse(
            code,
            name,
            activeImageUrl,
            inactiveImageUrl
        );
    }
}
