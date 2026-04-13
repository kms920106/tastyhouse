package com.tastyhouse.webapi.place.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이스 배너 이미지 응답")
public record PlaceBannerResponse(
    @Schema(description = "이미지 ID", example = "1")
    Long id,

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    String imageUrl,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {
    public static PlaceBannerResponse from(
        Long id,
        String imageUrl,
        Integer sort
    ) {
        return new PlaceBannerResponse(
            id,
            imageUrl,
            sort
        );
    }
}
