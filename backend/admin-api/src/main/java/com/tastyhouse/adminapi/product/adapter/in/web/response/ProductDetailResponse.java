package com.tastyhouse.adminapi.product.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductDetailResult;

@Schema(description = "상품 상세 응답")
public record ProductDetailResponse(
    @Schema(description = "상품 ID", example = "1")
    Long id,

    @Schema(description = "매장 ID", example = "1")
    Long shopId,

    @Schema(description = "카테고리 ID", example = "1")
    Long productCategoryId,

    @Schema(description = "상품명", example = "치즈불닭볶음면")
    String name,

    @Schema(description = "상품 설명", example = "매콤한 불닭볶음면에 치즈를 더했습니다")
    String description,

    @Schema(description = "정가", example = "8900")
    Integer originalPrice,

    @Schema(description = "할인가", example = "7900")
    Integer discountPrice,

    @Schema(description = "할인율", example = "0.11")
    BigDecimal discountRate,

    @Schema(description = "평점", example = "4.5")
    Double rating,

    @Schema(description = "리뷰 수", example = "12")
    Integer reviewCount,

    @Schema(description = "대표 상품 여부", example = "false")
    boolean representative,

    @Schema(description = "맵기", example = "2")
    Integer spiciness,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut,

    @Schema(description = "노출 여부", example = "true")
    boolean visible,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "생성일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정일시", example = "2026-01-02T00:00:00")
    LocalDateTime updatedAt
) {
    public static ProductDetailResponse from(ProductDetailResult result) {
        return new ProductDetailResponse(
            result.id(),
            result.shopId(),
            result.productCategoryId(),
            result.name(),
            result.description(),
            result.originalPrice(),
            result.discountPrice(),
            result.discountRate(),
            result.rating(),
            result.reviewCount(),
            result.representative(),
            result.spiciness(),
            result.soldOut(),
            result.visible(),
            result.sort(),
            result.createdAt(),
            result.updatedAt()
        );
    }
}
