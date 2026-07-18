package com.tastyhouse.adminapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 옵션 응답")
public record ProductOptionResponse(
    @Schema(description = "옵션 ID", example = "10")
    Long id,

    @Schema(description = "옵션명", example = "매운맛")
    String name,

    @Schema(description = "추가 금액", example = "500")
    Integer additionalPrice,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut
) {
    public static ProductOptionResponse from(
        Long id,
        String name,
        Integer additionalPrice,
        boolean soldOut
    ) {
        return new ProductOptionResponse(
            id,
            name,
            additionalPrice,
            soldOut
        );
    }
}
