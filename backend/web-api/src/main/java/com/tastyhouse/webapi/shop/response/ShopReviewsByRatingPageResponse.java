package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "평점별 매장 리뷰 페이지네이션 응답")
public record ShopReviewsByRatingPageResponse(
    @Schema(description = "평점별 리뷰 응답")
    ShopReviewsByRatingResponse response,
    @Schema(description = "전체 리뷰 개수", example = "42")
    long totalElements
) {
}
