package com.tastyhouse.adminapi.product.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 목록 항목 응답")
public record ProductListItemResponse(
    @Schema(description = "상품 ID", example = "1")
    Long id,

    @Schema(description = "매장명", example = "맛있는 분식")
    String shopName,

    @Schema(description = "상품명", example = "치즈불닭볶음면")
    String name,

    @Schema(description = "정가", example = "8900")
    Integer originalPrice,

    @Schema(description = "할인가", example = "7900")
    Integer discountPrice,

    @Schema(description = "할인율", example = "0.11")
    BigDecimal discountRate,

    @Schema(description = "대표 상품 여부", example = "false")
    boolean representative,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut,

    @Schema(description = "노출 여부", example = "true")
    boolean visible,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {
    public static ProductListItemResponse from(
        Long id,
        String shopName,
        String name,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        boolean representative,
        boolean soldOut,
        boolean visible,
        Integer sort
    ) {
        return new ProductListItemResponse(
            id,
            shopName,
            name,
            originalPrice,
            discountPrice,
            discountRate,
            representative,
            soldOut,
            visible,
            sort
        );
    }
}
