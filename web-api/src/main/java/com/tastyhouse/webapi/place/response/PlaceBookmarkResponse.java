package com.tastyhouse.webapi.place.response;

public record PlaceBookmarkResponse(
    boolean isBookmarked
) {
    public static PlaceBookmarkResponse from(boolean isBookmarked) {
    return new PlaceBookmarkResponse(isBookmarked);
    }
}
