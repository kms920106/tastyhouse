package com.tastyhouse.application.review.port.out.write;

public record ReviewLikeState(
    Long id,
    Long reviewId,
    Long memberId
) {
}
