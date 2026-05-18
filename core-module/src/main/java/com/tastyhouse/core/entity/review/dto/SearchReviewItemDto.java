package com.tastyhouse.core.entity.review.dto;

import com.querydsl.core.annotations.QueryProjection;

public record SearchReviewItemDto(
    Long id,
    String imageFilePath
) {
    @QueryProjection
    public SearchReviewItemDto {
    }
}
