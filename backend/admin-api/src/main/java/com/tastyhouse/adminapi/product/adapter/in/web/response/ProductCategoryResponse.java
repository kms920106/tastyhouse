package com.tastyhouse.adminapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductCategoryResult;

@Schema(description = "상품 카테고리 응답")
public record ProductCategoryResponse(
    @Schema(description = "카테고리 ID", example = "1")
    Long id,

    @Schema(description = "매장 ID", example = "1")
    Long shopId,

    @Schema(description = "카테고리명", example = "면류")
    String name,

    @Schema(description = "카테고리 설명. 미설정이면 null", example = "국물 있는 면 요리")
    String description,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "노출 여부", example = "true")
    boolean visible
) {
    public static ProductCategoryResponse from(ProductCategoryResult result) {
        return new ProductCategoryResponse(
            result.id(),
            result.shopId(),
            result.name(),
            result.description(),
            result.sort(),
            result.visible()
        );
    }
}
