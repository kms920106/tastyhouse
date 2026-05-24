package com.tastyhouse.core.domain.review.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;

public record MyReviewListItemResult(
    Long id,
    String imageUrl
) {
    @QueryProjection
    public MyReviewListItemResult {
    }
}
