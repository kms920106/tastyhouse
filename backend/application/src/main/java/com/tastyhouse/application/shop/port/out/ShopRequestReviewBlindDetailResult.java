package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public record ShopRequestReviewBlindDetailResult(
    Long reviewId,
    ReviewBlindReason reason,
    String detailReason,
    String reviewContent,
    Double reviewTotalRating,
    ApprovalStatus status,
    String rejectReason
) {
}
