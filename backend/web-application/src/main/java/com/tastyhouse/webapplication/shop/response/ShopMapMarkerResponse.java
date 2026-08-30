package com.tastyhouse.webapplication.shop.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지도 마커 응답")
public record ShopMapMarkerResponse(
    @Schema(description = "가게 ID", example = "1")
    Long id,

    @Schema(description = "위도", example = "37.5013")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.0396")
    BigDecimal longitude,

    @Schema(description = "상호명", example = "맛있는 집")
    String name
) {
    public static ShopMapMarkerResponse from(
        Long id,
        BigDecimal latitude,
        BigDecimal longitude,
        String name
    ) {
        return new ShopMapMarkerResponse(
            id,
            latitude,
            longitude,
            name
        );
    }
}
