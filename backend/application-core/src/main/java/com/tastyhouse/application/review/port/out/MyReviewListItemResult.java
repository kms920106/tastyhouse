package com.tastyhouse.application.review.port.out;

public record MyReviewListItemResult(
    Long id,
    String imageUrl,
    boolean ownerOnly
) {
}
