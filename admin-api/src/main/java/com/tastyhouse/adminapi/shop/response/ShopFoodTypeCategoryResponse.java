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

    @Schema(description = "활성 상태 아이콘 파일 ID", example = "22")
    Long activeImageFileId,

    @Schema(description = "비활성 상태 아이콘 파일 ID", example = "23")
    Long inactiveImageFileId,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "사용 여부", example = "true")
    boolean visible
) {
    public static ShopFoodTypeCategoryResponse from(
        Long id,
        String foodType,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopFoodTypeCategoryResponse(
            id,
            foodType,
            displayName,
            activeImageFileId,
            inactiveImageFileId,
            sort,
            visible
        );
    }
}
