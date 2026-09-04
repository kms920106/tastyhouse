package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryViewResult;

@Schema(description = "가게 포토 카테고리 응답")
public record ShopPhotoCategoryResponse(
    @Schema(description = "카테고리명", example = "외부")
    String name,

    @Schema(description = "이미지 URL 목록")
    List<String> imageUrls
) {
    public static ShopPhotoCategoryResponse from(ShopPhotoCategoryViewResult result) {
        return new ShopPhotoCategoryResponse(
            result.name(),
            result.imageUrls()
        );
    }
}
