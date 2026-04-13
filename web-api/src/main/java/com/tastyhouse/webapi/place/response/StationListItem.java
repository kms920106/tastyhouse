package com.tastyhouse.webapi.place.response;

public record StationListItem(
    Long id,
    String name
) {
    public static StationListItem from(
        Long id,
        String name
    ) {
        return new StationListItem(
            id,
            name
        );
    }
}
