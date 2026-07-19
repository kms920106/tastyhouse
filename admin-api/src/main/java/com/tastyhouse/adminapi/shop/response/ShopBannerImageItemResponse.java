package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 배너 이미지 응답")
public record ShopBannerImageItemResponse(
    @Schema(description = "배너 이미지 ID", example = "1")
    Long id,

    @Schema(description = "이미지 파일 ID", example = "30")
    Long imageFileId,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {
    public static ShopBannerImageItemResponse from(Long id, Long imageFileId, Integer sort) {
        return new ShopBannerImageItemResponse(id, imageFileId, sort);
    }
}
