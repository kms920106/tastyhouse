package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "편의시설 정보")
public record AmenityItem(
    @Schema(description = "편의시설 코드", example = "PARKING")
    String code,

    @Schema(description = "편의시설 표시명", example = "주차 가능")
    String name,

    @Schema(description = "편의시설 활성 이미지 URL", example = "https://example.com/parking-on.png")
    String activeImageUrl
) {
    public static AmenityItem from(
        String code,
        String name,
        String activeImageUrl
    ) {
        return new AmenityItem(
            code,
            name,
            activeImageUrl
        );
    }
}
