package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "음식종류 카테고리 응답")
public record ShopFoodTypeCategoryResponse(
    @Schema(description = "카테고리 ID", example = "1")
    Long id,

    @Schema(description = "음식 유형", example = "KOREAN")
    String foodType,

    @Schema(description = "화면 표시명", example = "한식")
    String displayName,

    @Schema(description = "활성 상태 아이콘 URL(없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Factive.png?alt=media")
    String activeImageUrl,

    @Schema(description = "비활성 상태 아이콘 URL(없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Finactive.png?alt=media")
    String inactiveImageUrl,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "사용 여부", example = "true")
    boolean visible
) {
    public static ShopFoodTypeCategoryResponse from(
        Long id,
        String foodType,
        String displayName,
        String activeImageUrl,
        String inactiveImageUrl,
        Integer sort,
        boolean visible
    ) {
        return new ShopFoodTypeCategoryResponse(
            id,
            foodType,
            displayName,
            activeImageUrl,
            inactiveImageUrl,
            sort,
            visible
        );
    }
}
