package com.tastyhouse.webapi.place.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "플레이스 메뉴 카테고리 응답")
public record PlaceMenuCategoryResponse(
    @Schema(description = "카테고리명", example = "대표 메뉴")
    String categoryName,

    @Schema(description = "메뉴 목록")
    List<PlaceMenuResponse> menus
) {
    public static PlaceMenuCategoryResponse from(
        String categoryName,
        List<PlaceMenuResponse> menus
    ) {
        return new PlaceMenuCategoryResponse(
            categoryName,
            menus
        );
    }
}
