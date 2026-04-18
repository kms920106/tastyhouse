package com.tastyhouse.webapi.place.response;

public record FoodTypeListItem(
    String code,
    String name,
    String imageUrl
) {
    public static FoodTypeListItem from(
    String code,
    String name,
    String imageUrl
    ) {
    return new FoodTypeListItem(
        code,
        name,
        imageUrl
    );
    }
}
