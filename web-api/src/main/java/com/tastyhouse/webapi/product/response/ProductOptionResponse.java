package com.tastyhouse.webapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "옵션")
public record ProductOptionResponse(
    @Schema(description = "옵션 ID", example = "1")
    Long id,

    @Schema(description = "옵션명", example = "많이 맵게")
    String name,

    @Schema(description = "추가 금액", example = "0")
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
        return new ProductOptionResponse(id, name, additionalPrice, soldOut);
    }
}
