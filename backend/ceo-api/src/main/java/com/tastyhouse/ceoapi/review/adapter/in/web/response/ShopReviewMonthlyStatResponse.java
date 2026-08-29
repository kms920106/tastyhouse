package com.tastyhouse.ceoapi.review.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "월별 리뷰 통계")
public record ShopReviewMonthlyStatResponse(
    @Schema(description = "연월(yyyy-MM)", example = "2026-06")
    String yearMonth,

    @Schema(description = "월 평균 종합 평점. 리뷰가 0건인 달은 null입니다.", example = "4.3")
    Double averageRating,

    @Schema(description = "월 리뷰 수", example = "12")
    Long reviewCount
) {

    public static ShopReviewMonthlyStatResponse from(
        String yearMonth,
        Double averageRating,
        Long reviewCount
    ) {
        return new ShopReviewMonthlyStatResponse(
            yearMonth,
            averageRating,
            reviewCount
        );
    }
}
