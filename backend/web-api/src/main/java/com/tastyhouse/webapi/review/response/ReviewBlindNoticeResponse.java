package com.tastyhouse.webapi.review.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시중단 리뷰 안내 응답 — 고객이 삭제 동의 여부를 판단하는 데 필요한 정보")
public record ReviewBlindNoticeResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,

    @Schema(description = "리뷰 내용", example = "정말 맛있어요")
    String content,

    @Schema(description = "리뷰 이미지 URL 목록")
    List<String> imageUrls,

    @Schema(description = "리뷰 작성일시", example = "2026-08-01T12:00:00")
    LocalDateTime createdAt,

    @Schema(description = "가게명", example = "맛있는 김밥")
    String shopName,

    @Schema(
        description = "게시중단 사유 코드",
        allowableValues = {"ADVERTISEMENT", "PROFANITY", "IRRELEVANT", "PRIVACY", "ETC"},
        example = "PROFANITY"
    )
    String reason,

    @Schema(description = "게시중단 사유 한글명", example = "욕설·비방")
    String reasonDescription,

    @Schema(description = "상세 사유. 입력하지 않았으면 null입니다.", example = "특정 직원을 지목한 욕설이 포함되어 있습니다.")
    String detailReason,

    @Schema(description = "재노출 예정일시 — 삭제에 동의하지 않으면 이 시각 이후 다시 노출됩니다.", example = "2026-09-16T14:30:00")
    LocalDateTime blindUntil
) {

    public static ReviewBlindNoticeResponse from(
        Long reviewId,
        String content,
        List<String> imageUrls,
        LocalDateTime createdAt,
        String shopName,
        String reason,
        String reasonDescription,
        String detailReason,
        LocalDateTime blindUntil
    ) {
        return new ReviewBlindNoticeResponse(
            reviewId,
            content,
            imageUrls,
            createdAt,
            shopName,
            reason,
            reasonDescription,
            detailReason,
            blindUntil
        );
    }
}
