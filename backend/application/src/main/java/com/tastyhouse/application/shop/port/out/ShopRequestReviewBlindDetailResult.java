package com.tastyhouse.application.shop.port.out;

public record ShopRequestReviewBlindDetailResult(
    Long reviewId,
    String reason,
    String reasonDescription,
    String detailReason,
    String reviewContent,
    Double reviewTotalRating,
    String status,
    String rejectReason
) {
}
