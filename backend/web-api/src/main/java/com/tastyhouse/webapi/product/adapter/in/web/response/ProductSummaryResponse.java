package com.tastyhouse.webapi.product.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;

@Schema(description = "상품 요약 응답")
public record ProductSummaryResponse(
    @Schema(description = "상품 ID", example = "1")
    Long id,

    @Schema(description = "상품명", example = "명란 크림 파스타")
    String name,

    @Schema(description = "이미지 URL", example = "https://example.com/menu.jpg")
    String imageUrl,

    @Schema(description = "원가", example = "18500")
    Integer originalPrice,

    @Schema(description = "할인가", example = "18000")
    Integer discountPrice,

    @Schema(description = "할인율", example = "10")
    BigDecimal discountRate,

    @Schema(description = "상품 평점", example = "3.5")
    Double rating,

    @Schema(description = "리뷰 수", example = "24")
    Integer reviewCount,

    @Schema(description = "대표 상품 여부", example = "true")
    boolean representative,

    @Schema(description = "매운맛 정도 (0-5 또는 0-10)", example = "3")
    Integer spiciness
) {
    public static ProductSummaryResponse from(SearchProductItemResult result) {
        return new ProductSummaryResponse(
            result.id(),
            result.name(),
            result.imageUrl(),
            result.originalPrice(),
            result.discountPrice(),
            result.discountRate(),
            result.rating(),
            result.reviewCount(),
            result.representative(),
            result.spiciness()
        );
    }

    public static ProductSummaryResponse from(ShopProductItemResult result) {
        return new ProductSummaryResponse(
            result.id(),
            result.name(),
            result.imageUrl(),
            result.originalPrice(),
            result.discountPrice(),
            result.discountRate(),
            result.rating(),
            result.reviewCount(),
            result.representative(),
            result.spiciness()
        );
    }
}
