package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;

/**
 * 관리자 게시중단 요청 심사 목록 항목.
 *
 * <p>{@code reviewContent}는 미리보기용 원문이다 — 심사자가 목록에서 바로 판단할 수 있어야 하므로
 * 별도 상세 진입 없이 함께 내린다. 길이 절단은 화면이 담당한다(서버가 자르면 상세와 값이 갈린다).
 *
 * <p>{@code blindUntil}(재노출 예정일시)은 목록에도 내린다 — 심사자가 "언제 풀리는 건인지"를 목록에서
 * 파악해야 하기 때문이다. 반면 <b>증빙 서류는 상세에서만 열람</b>하므로 이 record에 두지 않는다.
 */
public record ReviewBlindRequestListItemResult(
    Long id,
    Long reviewId,
    Long shopId,
    String shopName,
    ReviewBlindReason reason,
    ReviewBlindStatus status,
    LocalDateTime blindUntil,
    String reviewContent,
    Double reviewTotalRating,
    LocalDateTime createdAt
) {
}
