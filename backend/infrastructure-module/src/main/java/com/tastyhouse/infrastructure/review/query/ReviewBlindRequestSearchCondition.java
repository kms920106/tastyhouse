package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDate;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;

/**
 * 관리자 게시중단 요청 심사 목록 조회 조건.
 */
public record ReviewBlindRequestSearchCondition(
    Long shopId,
    ReviewBlindStatus status,
    ReviewBlindReason reason,
    LocalDate startDate,
    LocalDate endDate
) {

    public static ReviewBlindRequestSearchCondition of(
        Long shopId,
        ReviewBlindStatus status,
        ReviewBlindReason reason,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new ReviewBlindRequestSearchCondition(shopId, status, reason, startDate, endDate);
    }
}
