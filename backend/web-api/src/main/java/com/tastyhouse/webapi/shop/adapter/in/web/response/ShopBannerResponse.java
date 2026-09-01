package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;

@Schema(description = "가게 배너 이미지 응답")
public record ShopBannerResponse(
    @Schema(description = "이미지 ID", example = "1")
    Long id,

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    String imageUrl,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {
    public static ShopBannerResponse from(ShopBannerImageResult result) {
        return new ShopBannerResponse(
            result.id(),
            result.imageUrl(),
            result.sort()
        );
    }
}
