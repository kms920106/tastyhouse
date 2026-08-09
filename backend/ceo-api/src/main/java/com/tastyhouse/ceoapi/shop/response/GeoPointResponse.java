package com.tastyhouse.ceoapi.shop.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 도형을 이루는 좌표 한 점(응답 경계).
 *
 * <p>요청과 대칭으로 {@code {latitude, longitude}} 객체다 — 근거는 {@code GeoPointRequest} 참고.
 */
@Schema(description = "좌표 한 점")
public record GeoPointResponse(
    @Schema(description = "위도", example = "37.500000")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.036000")
    BigDecimal longitude
) {

    public static GeoPointResponse from(
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        return new GeoPointResponse(
            latitude,
            longitude
        );
    }
}
