package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopFoodTypeAssignmentResult;

@Schema(description = "가게 음식종류 지정 응답")
public record ShopFoodTypeResponse(
    @Schema(description = "지정 ID", example = "1")
    Long id,

    @Schema(description = "음식종류 카테고리 ID", example = "5")
    Long foodTypeCategoryId,

    @Schema(description = "음식종류 유형", example = "KOREAN")
    String foodType,

    @Schema(description = "화면 표시명", example = "한식")
    String displayName,

    @Schema(description = "활성 상태 아이콘 파일 경로", example = "https://cdn.example.com/food-type/korean-active.png")
    String activeFilePath
) {
    public static ShopFoodTypeResponse from(ShopFoodTypeAssignmentResult result) {
        return new ShopFoodTypeResponse(
            result.id(),
            result.foodTypeCategoryId(),
            result.foodType().name(),
            result.displayName(),
            result.activeIconUrl()
        );
    }
}
