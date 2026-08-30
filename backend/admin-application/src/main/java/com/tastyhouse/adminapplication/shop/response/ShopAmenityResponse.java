package com.tastyhouse.adminapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 편의시설 지정 응답")
public record ShopAmenityResponse(
    @Schema(description = "지정 ID", example = "1")
    Long id,

    @Schema(description = "편의시설 카테고리 ID", example = "3")
    Long amenityCategoryId,

    @Schema(description = "편의시설 유형", example = "PARKING")
    String amenity,

    @Schema(description = "화면 표시명", example = "주차")
    String displayName,

    @Schema(description = "활성 상태 아이콘 파일 경로", example = "https://cdn.example.com/amenity/parking-active.png")
    String activeFilePath
) {
    public static ShopAmenityResponse from(
        Long id,
        Long amenityCategoryId,
        String amenity,
        String displayName,
        String activeFilePath
    ) {
        return new ShopAmenityResponse(
            id,
            amenityCategoryId,
            amenity,
            displayName,
            activeFilePath
        );
    }
}
