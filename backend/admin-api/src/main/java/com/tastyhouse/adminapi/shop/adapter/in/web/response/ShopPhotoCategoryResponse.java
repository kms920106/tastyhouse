package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;

@Schema(description = "가게 포토 카테고리 응답")
public record ShopPhotoCategoryResponse(
    @Schema(description = "카테고리 ID", example = "1")
    Long id,

    @Schema(description = "포토 카테고리명", example = "가게 외관")
    String name
) {
    public static ShopPhotoCategoryResponse from(ShopPhotoCategoryResult result) {
        return new ShopPhotoCategoryResponse(
            result.id(),
            result.name()
        );
    }
}
