package com.tastyhouse.webapi.place.response;

import java.util.List;
import java.util.Map;

public record PlaceReviewsByRatingResponse(
    Map<Integer, List<PlaceReviewListItemResponse>> reviewsByRating,
    List<PlaceReviewListItemResponse> allReviews,
    Long totalReviewCount
) {
    public static PlaceReviewsByRatingResponse from(
    Map<Integer, List<PlaceReviewListItemResponse>> reviewsByRating,
    List<PlaceReviewListItemResponse> allReviews,
    Long totalReviewCount
    ) {
    return new PlaceReviewsByRatingResponse(
        reviewsByRating,
        allReviews,
        totalReviewCount
    );
    }
}
