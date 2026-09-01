package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopAmenityWithCategoryResult;

@Schema(description = "편의시설 정보")
public record ShopAmenityItem(
    @Schema(description = "편의시설 코드", example = "PARKING")
    String code,

    @Schema(description = "편의시설 표시명", example = "주차 가능")
    String name,

    @Schema(description = "편의시설 활성 이미지 URL", example = "https://example.com/parking-on.png")
    String activeImageUrl
) {
    public static ShopAmenityItem from(ShopAmenityWithCategoryResult result) {
        return new ShopAmenityItem(
            result.amenity().name(),
            result.displayName(),
            result.activeIconUrl()
        );
    }
}
