package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;

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
    public static ShopAmenityListItemResponse from(ShopAmenityCategoryResult result) {
        return new ShopAmenityListItemResponse(
            result.amenity().name(),
            result.displayName(),
            result.activeIconUrl(),
            result.inactiveIconUrl()
        );
    }
}
