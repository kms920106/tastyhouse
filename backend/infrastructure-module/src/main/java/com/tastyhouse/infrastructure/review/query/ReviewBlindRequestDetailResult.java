package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 관리자 게시중단 요청 심사 상세.
 *
 * <p>{@code reviewHidden}은 심사 판단에 필요하다 — 이미 숨겨진 리뷰라면(관리자가 직접 숨겼거나 다른
 * 요청이 먼저 승인됐거나) 이 요청을 승인해도 상태가 바뀌지 않는다는 것을 심사자가 알아야 한다.
 *
 * <p>{@code reviewImageUrls}는 다건이라 본 쿼리에 join하지 않고 별도 조회 후 위더로 채운다.
 */
public record ReviewBlindRequestDetailResult(
    Long id,
    Long reviewId,
    Long shopId,
    String shopName,
    ReviewBlindReason reason,
    String detailReason,
    ApprovalStatus status,
    String rejectReason,
    String reviewContent,
    Double reviewTotalRating,
    List<String> reviewImageUrls,
    String reviewMemberNickname,
    boolean reviewHidden,
    LocalDateTime reviewCreatedAt,
    LocalDateTime createdAt
) {

    public ReviewBlindRequestDetailResult withReviewImageUrls(List<String> reviewImageUrls) {
        return new ReviewBlindRequestDetailResult(
            this.id,
            this.reviewId,
            this.shopId,
            this.shopName,
            this.reason,
            this.detailReason,
            this.status,
            this.rejectReason,
            this.reviewContent,
            this.reviewTotalRating,
            reviewImageUrls,
            this.reviewMemberNickname,
            this.reviewHidden,
            this.reviewCreatedAt,
            this.createdAt
        );
    }
}
