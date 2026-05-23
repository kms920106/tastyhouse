package com.tastyhouse.webapi.search.response;

public record SearchPlaceListItemResponse(
    Long placeId,
    Long bookmarkId,
    String placeName,
    String stationName,
    Double rating,
    String imageUrl,
    boolean bookmarked
) {
    public static SearchPlaceListItemResponse from(
        Long placeId,
        String placeName,
        String stationName,
        Double rating,
        String imageUrl,
        boolean bookmarked
    ) {
        return new SearchPlaceListItemResponse(
            placeId,
            null,
            placeName,
            stationName,
            rating,
            imageUrl,
            bookmarked
        );
    }
}
