package com.tastyhouse.webapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "음식 타입 목록 아이템 응답")
public record ShopFoodTypeListItemResponse(
    @Schema(description = "음식 타입 코드", example = "KOREAN")
    String code,
    @Schema(description = "음식 타입 이름", example = "한식")
    String name,
    @Schema(description = "활성 상태 아이콘 이미지 URL", example = "https://cdn.tastyhouse.com/food-type/korean-active.png")
    String activeImageUrl,
    @Schema(description = "비활성 상태 아이콘 이미지 URL", example = "https://cdn.tastyhouse.com/food-type/korean-inactive.png")
    String inactiveImageUrl
) {
    public static ShopFoodTypeListItemResponse from(
        String code,
        String name,
        String activeImageUrl,
        String inactiveImageUrl
    ) {
        return new ShopFoodTypeListItemResponse(
            code,
            name,
            activeImageUrl,
            inactiveImageUrl
        );
    }
}
