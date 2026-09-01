package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;

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
    public static ShopAmenityResponse from(ShopAmenityAssignmentResult result) {
        return new ShopAmenityResponse(
            result.id(),
            result.amenityCategoryId(),
            result.amenity().name(),
            result.displayName(),
            result.activeIconUrl()
        );
    }
}
