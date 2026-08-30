package com.tastyhouse.webapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "편의시설 목록 항목 응답")
public record ShopAmenityListItemResponse(
    @Schema(description = "편의시설 코드", example = "PARKING")
    String code,

    @Schema(description = "편의시설명", example = "주차 가능")
    String name,

    @Schema(description = "활성 상태 아이콘 이미지 URL", example = "https://cdn.tastyhouse.com/amenity/parking-active.png")
    String activeImageUrl,

    @Schema(description = "비활성 상태 아이콘 이미지 URL", example = "https://cdn.tastyhouse.com/amenity/parking-inactive.png")
    String inactiveImageUrl
) {
    public static ShopAmenityListItemResponse from(
        String code,
        String name,
        String activeImageUrl,
        String inactiveImageUrl
    ) {
        return new ShopAmenityListItemResponse(
            code,
            name,
            activeImageUrl,
            inactiveImageUrl
        );
    }
}
