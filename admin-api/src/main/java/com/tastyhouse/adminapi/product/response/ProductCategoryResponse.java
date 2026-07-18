package com.tastyhouse.adminapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 카테고리 응답")
public record ProductCategoryResponse(
    @Schema(description = "카테고리 ID", example = "1")
    Long id,

    @Schema(description = "매장 ID", example = "1")
    Long shopId,

    @Schema(description = "카테고리명", example = "면류")
    String name,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "노출 여부", example = "true")
    boolean visible
) {
    public static ProductCategoryResponse from(
        Long id,
        Long shopId,
        String name,
        Integer sort,
        boolean visible
    ) {
        return new ProductCategoryResponse(id, shopId, name, sort, visible);
    }
}
