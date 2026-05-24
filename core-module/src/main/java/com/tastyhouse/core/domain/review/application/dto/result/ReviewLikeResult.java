package com.tastyhouse.core.domain.review.application.dto.result;

public record ReviewLikeResult(
    Long reviewId,
    boolean liked
) {
}
