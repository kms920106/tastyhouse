package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 요청처리 현황 상세의 리뷰 게시중단 원본 투영.
 *
 * <p>{@code status}·{@code rejectReason}의 진실원이라 함께 담는다(인덱스는 파생 읽기모델).
 */
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
