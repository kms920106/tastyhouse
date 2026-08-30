package com.tastyhouse.webapplication.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 리뷰 수")
public record ProductReviewCountResponse(
    @Schema(description = "리뷰 수", example = "128")
    Integer reviewCount
) {
    public static ProductReviewCountResponse from(Integer reviewCount) {
        return new ProductReviewCountResponse(reviewCount);
    }
}
