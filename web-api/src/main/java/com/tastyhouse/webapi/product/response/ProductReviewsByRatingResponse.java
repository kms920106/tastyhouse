package com.tastyhouse.webapi.product.response;

import java.util.List;
import java.util.Map;

public record ProductReviewsByRatingResponse(
    Map<Integer, List<ProductReviewListItem>> reviewsByRating,
    List<ProductReviewListItem> allReviews,
    Long totalReviewCount
) {
    public static ProductReviewsByRatingResponse from(
        Map<Integer, List<ProductReviewListItem>> reviewsByRating,
        List<ProductReviewListItem> allReviews,
        Long totalReviewCount
    ) {
        return new ProductReviewsByRatingResponse(
            reviewsByRating,
            allReviews,
            totalReviewCount
        );
    }
}
