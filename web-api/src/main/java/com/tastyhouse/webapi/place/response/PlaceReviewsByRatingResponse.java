package com.tastyhouse.webapi.place.response;

import java.util.List;
import java.util.Map;

public record PlaceReviewsByRatingResponse(
    Map<Integer, List<PlaceReviewListItem>> reviewsByRating,
    List<PlaceReviewListItem> allReviews,
    Long totalReviewCount
) {
    public static PlaceReviewsByRatingResponse from(
    Map<Integer, List<PlaceReviewListItem>> reviewsByRating,
    List<PlaceReviewListItem> allReviews,
    Long totalReviewCount
    ) {
    return new PlaceReviewsByRatingResponse(
        reviewsByRating,
        allReviews,
        totalReviewCount
    );
    }
}
