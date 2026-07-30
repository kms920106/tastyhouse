package com.tastyhouse.infrastructure.review.query;

import com.querydsl.core.annotations.QueryProjection;

public record BestReviewListItemResult(
    Long id,
    String imageUrl,
    String stationName,
    String shopName,
    String productName,
    Double totalRating,
    String content
) {
    @QueryProjection
    public BestReviewListItemResult {
    }
}
