package com.tastyhouse.core.entity.review.dto;

import com.querydsl.core.annotations.QueryProjection;

public record MyReviewListItemDto(
    Long id,
    String imageUrl
) {
    @QueryProjection
    public MyReviewListItemDto {
    }
}
