package com.tastyhouse.core.entity.review.dto;

import com.querydsl.core.annotations.QueryProjection;

public record SearchReviewItemDto(
    Long reviewId,
    String imageFilePath,
    Long placeId
) {
    @QueryProjection
    public SearchReviewItemDto {
    }
}
