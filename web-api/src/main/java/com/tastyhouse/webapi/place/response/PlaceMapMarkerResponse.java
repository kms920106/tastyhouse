package com.tastyhouse.webapi.place.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "지도 마커 응답")
public record PlaceMapMarkerResponse(
    @Schema(description = "플레이스 ID", example = "1")
    Long id,

    @Schema(description = "위도", example = "37.5013")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.0396")
    BigDecimal longitude,

    @Schema(description = "상호명", example = "맛있는 집")
    String name
) {
    public static PlaceMapMarkerResponse from(
        Long id,
        BigDecimal latitude,
        BigDecimal longitude,
        String name
    ) {
        return new PlaceMapMarkerResponse(
            id,
            latitude,
            longitude,
            name
        );
    }
}
