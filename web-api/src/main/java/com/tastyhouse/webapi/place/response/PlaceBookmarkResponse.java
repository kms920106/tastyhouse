package com.tastyhouse.webapi.place.response;

public record PlaceBookmarkResponse(
    boolean bookmarked
) {
    public static PlaceBookmarkResponse from(boolean bookmarked) {
    return new PlaceBookmarkResponse(bookmarked);
    }
}
