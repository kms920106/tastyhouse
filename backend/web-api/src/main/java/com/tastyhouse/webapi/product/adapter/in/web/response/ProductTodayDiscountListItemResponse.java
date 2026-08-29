package com.tastyhouse.webapi.product.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘의 할인 상품 목록 아이템 응답")
public record ProductTodayDiscountListItemResponse(
    @Schema(description = "상품 ID", example = "1")
    Long id,
    @Schema(description = "매장명", example = "테이스티하우스 강남점")
    String shopName,
    @Schema(description = "상품명", example = "시그니처 파스타")
    String name,
    @Schema(description = "상품 이미지 URL", example = "https://cdn.tastyhouse.com/product/1/image.jpg")
    String imageUrl,
    @Schema(description = "정가", example = "15000")
    Integer originalPrice,
    @Schema(description = "할인가", example = "12000")
    Integer discountPrice,
    @Schema(description = "할인율", example = "20.00")
    BigDecimal discountRate
) {
    public static ProductTodayDiscountListItemResponse from(
        Long id,
        String shopName,
        String name,
        String imageUrl,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate
    ) {
        return new ProductTodayDiscountListItemResponse(
            id,
            shopName,
            name,
            imageUrl,
            originalPrice,
            discountPrice,
            discountRate
        );
    }
}
