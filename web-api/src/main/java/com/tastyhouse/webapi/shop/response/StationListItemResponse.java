package com.tastyhouse.webapi.shop.response;

public record StationListItemResponse(
    Long id,
    String name
) {
    public static StationListItemResponse from(
        Long id,
        String name
    ) {
        return new StationListItemResponse(
            id,
            name
        );
    }
}
