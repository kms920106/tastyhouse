package com.tastyhouse.webapi.member.response;

public record PlaceBookmarkListItemResponse(
    Long placeId,
    Long bookmarkId,
    String placeName,
    String stationName,
    Double rating,
    String imageUrl,
    boolean bookmarked
) {
    public static PlaceBookmarkListItemResponse from(
        Long placeId,
        Long bookmarkId,
        String placeName,
        String stationName,
        Double rating,
        String imageUrl,
        boolean bookmarked
    ) {
        return new PlaceBookmarkListItemResponse(
            placeId,
            bookmarkId,
            placeName,
            stationName,
            rating,
            imageUrl,
            bookmarked
        );
    }
}
