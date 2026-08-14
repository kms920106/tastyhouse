package com.tastyhouse.ceoapi.review.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 게시중단 요청 이력 항목")
public record ReviewBlindRequestHistoryResponse(
    @Schema(description = "게시중단 요청 ID", example = "31")
    Long id,

    @Schema(
        description = "요청 사유 코드",
        allowableValues = {"ADVERTISEMENT", "PROFANITY", "IRRELEVANT", "PRIVACY", "ETC"},
        example = "PROFANITY"
    )
    String reason,

    @Schema(description = "요청 사유 한글명", example = "욕설·비방")
    String reasonDescription,

    @Schema(description = "상세 사유. 입력하지 않았으면 null입니다.", example = "특정 직원을 지목한 욕설이 포함되어 있습니다.")
    String detailReason,

    @Schema(
        description = "처리 상태",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"},
        example = "REJECTED"
    )
    String status,

    @Schema(description = "처리 상태 한글명", example = "반려")
    String statusDescription,

    @Schema(description = "반려 사유. 반려된 요청에만 값이 있습니다.", example = "게시중단 기준에 해당하지 않습니다.")
    String rejectReason,

    @Schema(description = "요청 접수일시", example = "2026-06-21T09:30:00")
    LocalDateTime createdAt
) {

    public static ReviewBlindRequestHistoryResponse from(
        Long id,
        String reason,
        String reasonDescription,
        String detailReason,
        String status,
        String statusDescription,
        String rejectReason,
        LocalDateTime createdAt
    ) {
        return new ReviewBlindRequestHistoryResponse(
            id,
            reason,
            reasonDescription,
            detailReason,
            status,
            statusDescription,
            rejectReason,
            createdAt
        );
    }
}
