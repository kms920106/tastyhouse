package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.GeoPointView;

@Schema(description = "좌표 한 점")
public record GeoPointResponse(
    @Schema(description = "위도", example = "37.500000")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.036000")
    BigDecimal longitude
) {
    public static GeoPointResponse from(GeoPointView point) {
        return new GeoPointResponse(
            point.latitude(),
            point.longitude()
        );
    }
}
