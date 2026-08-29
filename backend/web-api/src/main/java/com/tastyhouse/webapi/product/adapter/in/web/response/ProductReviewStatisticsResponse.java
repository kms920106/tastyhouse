package com.tastyhouse.webapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 리뷰 통계 응답")
public record ProductReviewStatisticsResponse(
    @Schema(description = "총 평점", example = "4.8")
    Double totalRating,

    @Schema(description = "리뷰 개수", example = "1024")
    Long totalReviewCount,

    @Schema(description = "맛 평점", example = "3.8")
    Double averageTasteRating,

    @Schema(description = "양 평점", example = "3.6")
    Double averageAmountRating,

    @Schema(description = "가격 평점", example = "3.9")
    Double averagePriceRating
) {
    public static ProductReviewStatisticsResponse from(
        Double totalRating,
        Long totalReviewCount,
        Double averageTasteRating,
        Double averageAmountRating,
        Double averagePriceRating
    ) {
        return new ProductReviewStatisticsResponse(
            totalRating,
            totalReviewCount,
            averageTasteRating,
            averageAmountRating,
            averagePriceRating
        );
    }
}
