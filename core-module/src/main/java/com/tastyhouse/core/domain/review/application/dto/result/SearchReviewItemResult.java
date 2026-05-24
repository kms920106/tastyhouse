package com.tastyhouse.core.domain.review.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;

public record SearchReviewItemResult(
    Long id,
    String imageFilePath
) {
    @QueryProjection
    public SearchReviewItemResult {
    }
}
