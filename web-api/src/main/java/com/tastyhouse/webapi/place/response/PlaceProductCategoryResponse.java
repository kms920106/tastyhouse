package com.tastyhouse.webapi.place.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "플레이스 상품 카테고리 응답")
public record PlaceProductCategoryResponse(
    @Schema(description = "카테고리명", example = "대표 상품")
    String categoryName,

    @Schema(description = "상품 목록")
    List<PlaceProductResponse> products
) {
    public static PlaceProductCategoryResponse from(
        String categoryName,
        List<PlaceProductResponse> menus
    ) {
        return new PlaceProductCategoryResponse(
            categoryName,
            menus
        );
    }
}
