package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;

/**
 * 관리자 게시중단 요청 심사 상세.
 *
 * <p>{@code reviewHidden}은 심사 판단에 필요하다 — 이미 숨겨진 리뷰라면(관리자가 직접 숨겼거나 다른
 * 요청이 먼저 승인됐거나) 이 요청을 승인해도 상태가 바뀌지 않는다는 것을 심사자가 알아야 한다.
 *
 * <p>{@code reviewImageUrls}·{@code attachmentUrls}는 다건이라 본 쿼리에 join하지 않고 별도 조회 후
 * 위더로 채운다.
 *
 * <p>{@code blindUntil}은 {@code APPROVED}가 아니면 {@code null}이다.
 */
public record ReviewBlindRequestDetailResult(
    Long id,
    Long reviewId,
    Long shopId,
    String shopName,
    ReviewBlindReason reason,
    String detailReason,
    ReviewBlindStatus status,
    String rejectReason,
    LocalDateTime blindUntil,
    String reviewContent,
    Double reviewTotalRating,
    List<String> reviewImageUrls,
    List<String> attachmentUrls,
    String reviewMemberNickname,
    boolean reviewHidden,
    LocalDateTime reviewCreatedAt,
    LocalDateTime createdAt
) {

    /**
     * 다건 조회 결과(리뷰 사진·증빙 서류 URL)를 채워 넣는다.
     *
     * <p><b>필드 선언 순서와 인자 순서를 한 필드씩 대조한다</b> — {@code String}·{@code LocalDateTime}·
     * {@code List<String>}이 각각 연속해 있어 자리를 바꿔도 컴파일되고 값만 조용히 뒤바뀐다
     * (DTO 조립 규칙의 경고).
     */
    public ReviewBlindRequestDetailResult withUrls(List<String> reviewImageUrls, List<String> attachmentUrls) {
        return new ReviewBlindRequestDetailResult(
            this.id,
            this.reviewId,
            this.shopId,
            this.shopName,
            this.reason,
            this.detailReason,
            this.status,
            this.rejectReason,
            this.blindUntil,
            this.reviewContent,
            this.reviewTotalRating,
            reviewImageUrls,
            attachmentUrls,
            this.reviewMemberNickname,
            this.reviewHidden,
            this.reviewCreatedAt,
            this.createdAt
        );
    }
}
