package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 포토 카테고리 이미지 응답")
public record ShopPhotoCategoryImageItemResponse(
    @Schema(description = "이미지 ID", example = "1")
    Long id,

    @Schema(description = "포토 카테고리 ID", example = "1")
    Long shopPhotoCategoryId,

    @Schema(description = "이미지 URL(없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Fphoto.jpg?alt=media")
    String imageUrl,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "노출 여부", example = "true")
    boolean visible
) {
    public static ShopPhotoCategoryImageItemResponse from(
        Long id,
        Long shopPhotoCategoryId,
        String imageUrl,
        Integer sort,
        boolean visible
    ) {
        return new ShopPhotoCategoryImageItemResponse(
            id,
            shopPhotoCategoryId,
            imageUrl,
            sort,
            visible
        );
    }
}
