package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 배너 이미지 응답")
public record ShopBannerImageItemResponse(
    @Schema(description = "배너 이미지 ID", example = "1")
    Long id,

    @Schema(description = "이미지 URL(없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Fbanner.jpg?alt=media")
    String imageUrl,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {
    public static ShopBannerImageItemResponse from(Long id, String imageUrl, Integer sort) {
        return new ShopBannerImageItemResponse(id, imageUrl, sort);
    }
}
