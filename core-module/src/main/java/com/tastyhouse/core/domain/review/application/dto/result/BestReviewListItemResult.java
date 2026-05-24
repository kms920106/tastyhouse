package com.tastyhouse.core.domain.review.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;

public record BestReviewListItemResult(
    Long id,
    String imageUrl,
    String stationName,
    String placeName,
    String productName,
    Double totalRating,
    String content
) {
    @QueryProjection
    public BestReviewListItemResult {
    }
}
