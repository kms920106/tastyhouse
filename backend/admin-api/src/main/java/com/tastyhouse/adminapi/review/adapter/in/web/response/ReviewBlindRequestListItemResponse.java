package com.tastyhouse.adminapi.review.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.ReviewBlindRequestListItemResult;

@Schema(description = "리뷰 게시중단 요청 목록 항목 응답")
public record ReviewBlindRequestListItemResponse(
    @Schema(description = "게시중단 요청 ID", example = "1")
    Long id,

    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,

    @Schema(description = "상점 ID", example = "1")
    Long shopId,

    @Schema(description = "상점명", example = "맛있는 김밥")
    String shopName,

    @Schema(description = "게시중단 요청 사유 코드", example = "ADVERTISEMENT")
    String reason,

    @Schema(description = "게시중단 요청 사유 설명", example = "광고·홍보")
    String reasonDescription,

    @Schema(description = "처리 상태 코드", example = "PENDING")
    String status,

    @Schema(description = "처리 상태 설명", example = "대기")
    String statusDescription,

    @Schema(description = "리뷰 내용", example = "정말 맛있어요")
    String reviewContent,

    @Schema(description = "리뷰 총 평점", example = "4.5")
    Double reviewTotalRating,

    @Schema(description = "재노출 예정일시(게시중단 상태일 때만 값 존재)", example = "2026-09-16T14:30:00")
    LocalDateTime blindUntil,

    @Schema(description = "요청 생성일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static ReviewBlindRequestListItemResponse from(ReviewBlindRequestListItemResult result) {
        return new ReviewBlindRequestListItemResponse(
            result.id(),
            result.reviewId(),
            result.shopId(),
            result.shopName(),
            result.reason().name(),
            result.reason().getDescription(),
            result.status().name(),
            result.status().getDescription(),
            result.reviewContent(),
            result.reviewTotalRating(),
            result.blindUntil(),
            result.createdAt()
        );
    }
}
