package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 포토 카테고리 이미지 응답")
public record ShopPhotoCategoryImageItemResponse(
    @Schema(description = "이미지 ID", example = "1")
    Long id,

    @Schema(description = "포토 카테고리 ID", example = "1")
    Long shopPhotoCategoryId,

    @Schema(description = "이미지 파일 ID", example = "31")
    Long imageFileId,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "노출 여부", example = "true")
    boolean visible
) {
    public static ShopPhotoCategoryImageItemResponse from(
        Long id,
        Long shopPhotoCategoryId,
        Long imageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopPhotoCategoryImageItemResponse(
            id,
            shopPhotoCategoryId,
            imageFileId,
            sort,
            visible
        );
    }
}
