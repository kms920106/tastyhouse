package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 배너 이미지 응답")
public record ShopBannerResponse(
    @Schema(description = "이미지 ID", example = "1")
    Long id,

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    String imageUrl,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {
    public static ShopBannerResponse from(
        Long id,
        String imageUrl,
        Integer sort
    ) {
        return new ShopBannerResponse(
            id,
            imageUrl,
            sort
        );
    }
}
