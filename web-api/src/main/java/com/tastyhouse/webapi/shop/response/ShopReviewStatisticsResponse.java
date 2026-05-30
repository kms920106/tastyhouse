package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "가게 리뷰 통계 응답")
public record ShopReviewStatisticsResponse(
    @Schema(description = "총 평점", example = "4.8")
    Double totalRating,

    @Schema(description = "리뷰 개수", example = "1024")
    Long totalReviewCount,

    @Schema(description = "맛 평점", example = "3.8")
    Double averageTasteRating,

    @Schema(description = "양 평점", example = "3.6")
    Double averageAmountRating,

    @Schema(description = "가격 평점", example = "3.9")
    Double averagePriceRating,

    @Schema(description = "분위기 평점", example = "3.8")
    Double averageAtmosphereRating,

    @Schema(description = "친절 평점", example = "3.6")
    Double averageKindnessRating,

    @Schema(description = "위생 평점", example = "3.9")
    Double averageHygieneRating,

    @Schema(description = "재방문 의사 비율 (%)", example = "87")
    Double willRevisitPercentage,

    @Schema(description = "월별 리뷰 수 (키: 월(1-12), 값: 리뷰 수)", example = "{\"1\": 120, \"2\": 100, \"3\": 150}")
    Map<Integer, Long> monthlyReviewCounts,

    @Schema(description = "평점별 리뷰 수 (키: 평점(1-5), 값: 리뷰 수)", example = "{\"1\": 10, \"2\": 20, \"3\": 50, \"4\": 300, \"5\": 644}")
    Map<Integer, Long> ratingCounts
) {
    public static ShopReviewStatisticsResponse from(
        Double totalRating,
        Long totalReviewCount,
        Double averageTasteRating,
        Double averageAmountRating,
        Double averagePriceRating,
        Double averageAtmosphereRating,
        Double averageKindnessRating,
        Double averageHygieneRating,
        Double willRevisitPercentage,
        Map<Integer, Long> monthlyReviewCounts,
        Map<Integer, Long> ratingCounts
    ) {
        return new ShopReviewStatisticsResponse(
            totalRating,
            totalReviewCount,
            averageTasteRating,
            averageAmountRating,
            averagePriceRating,
            averageAtmosphereRating,
            averageKindnessRating,
            averageHygieneRating,
            willRevisitPercentage,
            monthlyReviewCounts,
            ratingCounts
        );
    }
}
