package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 포토 카테고리 응답")
public record ShopPhotoCategoryResponse(
    @Schema(description = "카테고리 ID", example = "1")
    Long id,

    @Schema(description = "포토 카테고리명", example = "가게 외관")
    String name
) {
    public static ShopPhotoCategoryResponse from(Long id, String name) {
        return new ShopPhotoCategoryResponse(id, name);
    }
}
