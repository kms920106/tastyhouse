package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;

@Schema(description = "가게 배너 이미지 응답")
public record ShopBannerImageItemResponse(
    @Schema(description = "배너 이미지 ID", example = "1")
    Long id,

    @Schema(description = "이미지 URL(없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Fbanner.jpg?alt=media")
    String imageUrl,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {
    public static ShopBannerImageItemResponse from(ShopBannerImageResult result) {
        return new ShopBannerImageItemResponse(
            result.id(),
            result.imageUrl(),
            result.sort()
        );
    }
}
