package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 관리자 게시중단 요청 심사 목록 항목.
 *
 * <p>{@code reviewContent}는 미리보기용 원문이다 — 심사자가 목록에서 바로 판단할 수 있어야 하므로
 * 별도 상세 진입 없이 함께 내린다. 길이 절단은 화면이 담당한다(서버가 자르면 상세와 값이 갈린다).
 */
public record ReviewBlindRequestListItemResult(
    Long id,
    Long reviewId,
    Long shopId,
    String shopName,
    ReviewBlindReason reason,
    ApprovalStatus status,
    String reviewContent,
    Double reviewTotalRating,
    LocalDateTime createdAt
) {
}
