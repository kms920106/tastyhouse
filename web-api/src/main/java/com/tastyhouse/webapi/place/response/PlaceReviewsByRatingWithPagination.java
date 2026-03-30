package com.tastyhouse.webapi.place.response;

public record PlaceReviewsByRatingWithPagination(
        PlaceReviewsByRatingResponse response,
        long totalElements
) {
}
