package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;

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
    public static ShopFoodTypeListItemResponse from(ShopFoodTypeCategoryResult result) {
        return new ShopFoodTypeListItemResponse(
            result.foodType().name(),
            result.displayName(),
            result.activeIconUrl(),
            result.inactiveIconUrl()
        );
    }
}
