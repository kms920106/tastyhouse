package com.tastyhouse.webapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배치 조회 옵션")
public record ProductBatchOptionResponse(
    @Schema(description = "옵션 ID", example = "1")
    Long id,

    @Schema(description = "옵션명", example = "라지")
    String name,

    @Schema(description = "옵션 추가 금액", example = "3000")
    Integer price
) {
    public static ProductBatchOptionResponse from(
        Long id,
        String name,
        Integer price
    ) {
        return new ProductBatchOptionResponse(
            id,
            name,
            price
        );
    }
}
