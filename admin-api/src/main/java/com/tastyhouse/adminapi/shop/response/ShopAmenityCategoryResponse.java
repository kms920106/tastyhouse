package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "편의시설 카테고리 응답")
public record ShopAmenityCategoryResponse(
    @Schema(description = "카테고리 ID", example = "1")
    Long id,

    @Schema(description = "편의시설 유형", example = "WIFI")
    String amenity,

    @Schema(description = "화면 표시명", example = "와이파이")
    String displayName,

    @Schema(description = "활성 상태 아이콘 파일 ID", example = "20")
    Long activeImageFileId,

    @Schema(description = "비활성 상태 아이콘 파일 ID", example = "21")
    Long inactiveImageFileId,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "사용 여부", example = "true")
    boolean visible
) {
    public static ShopAmenityCategoryResponse from(
        Long id,
        String amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopAmenityCategoryResponse(
            id,
            amenity,
            displayName,
            activeImageFileId,
            inactiveImageFileId,
            sort,
            visible
        );
    }
}
