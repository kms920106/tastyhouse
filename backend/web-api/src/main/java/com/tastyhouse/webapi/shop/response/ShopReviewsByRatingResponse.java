package com.tastyhouse.webapi.shop.response;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "평점별 매장 리뷰 응답")
public record ShopReviewsByRatingResponse(
    @Schema(description = "평점(1~5)별 리뷰 목록 맵")
    Map<Integer, List<ShopReviewListItemResponse>> reviewsByRating,
    @Schema(description = "전체 리뷰 목록")
    List<ShopReviewListItemResponse> allReviews,
    @Schema(description = "전체 리뷰 개수", example = "42")
    Long totalReviewCount
) {
    public static ShopReviewsByRatingResponse from(
        Map<Integer, List<ShopReviewListItemResponse>> reviewsByRating,
        List<ShopReviewListItemResponse> allReviews,
        Long totalReviewCount
    ) {
        return new ShopReviewsByRatingResponse(
            reviewsByRating,
            allReviews,
            totalReviewCount
        );
    }
}
