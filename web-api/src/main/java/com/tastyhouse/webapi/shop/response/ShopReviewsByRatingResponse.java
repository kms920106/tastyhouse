package com.tastyhouse.webapi.shop.response;

import java.util.List;
import java.util.Map;

public record ShopReviewsByRatingResponse(
    Map<Integer, List<ShopReviewListItemResponse>> reviewsByRating,
    List<ShopReviewListItemResponse> allReviews,
    Long totalReviewCount
) {
    public static ShopReviewsByRatingResponse from(
        Map<Integer, List<ShopReviewListItemResponse>> reviewsByRating,
        List<ShopReviewListItemResponse> allReviews,
        Long totalReviewCount
    ) {
        return new ShopReviewsByRatingResponse(
            reviewsByRating,
            allReviews,
            totalReviewCount
        );
    }
}
