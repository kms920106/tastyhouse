package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 리뷰 1건의 게시중단 요청 이력 항목.
 *
 * <p>{@code public}이어야 한다 — package-private이면 {@code Projections.constructor}가 public 생성자만
 * 탐색하므로 런타임에 {@code No constructor found}로 깨진다({@code QueryResultRecordVisibilityTest}가 강제).
 */
public record ReviewBlindRequestHistoryResult(
    Long id,
    ReviewBlindReason reason,
    String detailReason,
    ApprovalStatus status,
    String rejectReason,
    LocalDateTime createdAt
) {
}
