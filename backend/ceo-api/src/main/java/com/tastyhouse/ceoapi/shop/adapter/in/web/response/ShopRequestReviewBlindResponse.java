package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopRequestReviewBlindDetailResult;

/**
 * 요청처리 현황 상세의 리뷰 게시중단 요청 서브 객체.
 *
 * <p>대상 리뷰의 내용을 함께 담는다 — 통합 요청처리 화면에서 "무엇의 게시중단을 요청했는지"를 리뷰 관리
 * 화면으로 이동하지 않고 확인할 수 있어야 한다.
 */
@Schema(description = "리뷰 게시중단 요청 상세")
public record ShopRequestReviewBlindResponse(

    @Schema(description = "대상 리뷰 ID", example = "482")
    Long reviewId,

    @Schema(
        description = "요청 사유 코드",
        example = "PROFANITY",
        allowableValues = {"ADVERTISEMENT", "PROFANITY", "IRRELEVANT", "PRIVACY", "ETC"}
    )
    String reason,

    @Schema(description = "요청 사유 한글 라벨", example = "욕설·비방")
    String reasonDescription,

    @Schema(description = "상세 사유. 입력하지 않았으면 null", example = "특정 직원을 지목한 욕설이 포함되어 있습니다.")
    String detailReason,

    @Schema(description = "대상 리뷰 내용", example = "국물이 진하고 맛있었어요.")
    String reviewContent,

    @Schema(description = "대상 리뷰 종합 평점", example = "1.0")
    Double reviewTotalRating
) {

    public static ShopRequestReviewBlindResponse from(ShopRequestReviewBlindDetailResult result) {
        return new ShopRequestReviewBlindResponse(
            result.reviewId(),
            result.reason().name(),
            result.reason().getDescription(),
            result.detailReason(),
            result.reviewContent(),
            result.reviewTotalRating()
        );
    }
}
