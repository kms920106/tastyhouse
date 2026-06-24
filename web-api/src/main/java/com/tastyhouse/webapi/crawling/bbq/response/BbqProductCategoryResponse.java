package com.tastyhouse.webapi.crawling.bbq.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "BBQ 상품 카테고리 응답")
public record BbqProductCategoryResponse(
    @Schema(description = "카테고리 ID", example = "1")
    Long id,

    @Schema(description = "플레이스 ID", example = "1")
    Long shopId,

    @Schema(description = "카테고리명", example = "시그니처 메뉴")
    String name,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "노출 여부", example = "true")
    boolean visible
) {
    public static BbqProductCategoryResponse from(
        Long id,
        Long shopId,
        String name,
        Integer sort,
        boolean visible
    ) {
        return new BbqProductCategoryResponse(
            id,
            shopId,
            name,
            sort,
            visible
        );
    }
}
