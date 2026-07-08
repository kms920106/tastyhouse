package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매장 북마크 여부 응답")
public record ShopBookmarkResponse(
    @Schema(description = "북마크 여부", example = "true")
    boolean bookmarked
) {
    public static ShopBookmarkResponse from(boolean bookmarked) {
        return new ShopBookmarkResponse(bookmarked);
    }
}
