package com.tastyhouse.core.entity.review.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BestReviewListItemDto(
    Long id,
    String imageUrl,
    String stationName,
    Double totalRating,
    String content
) {
    @QueryProjection
    public BestReviewListItemDto {
    }
}
