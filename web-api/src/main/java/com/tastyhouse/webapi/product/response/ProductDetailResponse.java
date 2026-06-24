package com.tastyhouse.webapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "상품 상세 정보")
public record ProductDetailResponse(
    @Schema(description = "상품 ID", example = "1")
    Long id,

    @Schema(description = "상품명", example = "명란 크림 파스타")
    String name,

    @Schema(description = "상품 설명", example = "신선한 명란과 크림소스의 조화")
    String description,

    @Schema(description = "원가", example = "18500")
    Integer originalPrice,

    @Schema(description = "할인가", example = "16650")
    Integer discountPrice,

    @Schema(description = "할인율", example = "10.00")
    BigDecimal discountRate,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut
) {
    public static ProductDetailResponse from(
        Long id,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        boolean soldOut
    ) {
        return new ProductDetailResponse(
            id,
            name,
            description,
            originalPrice,
            discountPrice,
            discountRate,
            soldOut
        );
    }
}
