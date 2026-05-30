package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "가게 포토 카테고리 응답")
public record ShopPhotoCategoryResponse(
    @Schema(description = "카테고리명", example = "외부")
    String name,

    @Schema(description = "이미지 URL 목록")
    List<String> imageUrls
) {
    public static ShopPhotoCategoryResponse from(
        String name,
        List<String> imageUrls
    ) {
        return new ShopPhotoCategoryResponse(
            name,
            imageUrls
        );
    }
}
