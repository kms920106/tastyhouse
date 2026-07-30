package com.tastyhouse.infrastructure.review.query;

import com.querydsl.core.annotations.QueryProjection;

public record SearchReviewItemResult(
    Long id,
    String imageFilePath
) {
    @QueryProjection
    public SearchReviewItemResult {
    }
}
