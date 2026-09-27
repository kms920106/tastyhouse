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

    public ShopRequestReviewBlindDetailResult withReasonDescription(String reasonDescription) {
        return new ShopRequestReviewBlindDetailResult(
            this.reviewId,
            this.reason,
            reasonDescription,
            this.detailReason,
            this.reviewContent,
            this.reviewTotalRating,
            this.status,
            this.rejectReason
        );
    }
}
