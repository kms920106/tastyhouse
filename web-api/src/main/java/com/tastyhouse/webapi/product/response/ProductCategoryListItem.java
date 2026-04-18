package com.tastyhouse.webapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 카테고리 목록 아이템")
public record ProductCategoryListItem(
    @Schema(description = "카테고리 ID", example = "1")
    Long id,

    @Schema(description = "카테고리 표시명", example = "시그니처 메뉴")
    String displayName,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {
    public static ProductCategoryListItem from(
    Long id,
    String displayName,
    Integer sort
    ) {
    return new ProductCategoryListItem(
        id,
        displayName,
        sort
    );
    }
}
