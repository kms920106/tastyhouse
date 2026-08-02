package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 사진 응답")
public record ShopPhotoResponse(
    @Schema(description = "이미지 ID", example = "1")
    Long id,

    @Schema(description = "이미지 URL", example = "https://example.com/photo.jpg")
    String imageUrl,

    @Schema(description = "이미지 카테고리 코드", example = "EXTERIOR")
    String categoryCode,

    @Schema(description = "이미지 카테고리명", example = "가게 외관")
    String categoryName,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {
}
