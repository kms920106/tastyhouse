package com.tastyhouse.webapplication.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "평점별 상품 리뷰 목록과 페이지네이션 정보 응답")
public record ProductReviewsByRatingPageResponse(
    @Schema(description = "평점별 상품 리뷰 응답")
    ProductReviewsByRatingResponse response,
    @Schema(description = "전체 리뷰 개수", example = "42")
    long totalElements
) {
}
