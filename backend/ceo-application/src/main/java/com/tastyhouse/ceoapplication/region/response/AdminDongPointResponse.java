package com.tastyhouse.ceoapplication.region.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 행정동 경계를 이루는 좌표 한 점.
 *
 * <p>배달지역 도형의 {@code GeoPointResponse}와 형태가 같지만 타입을 공유하지 않는다 — 그쪽은
 * {@code shop} 도메인의 응답이고 이쪽은 {@code region} 도메인의 응답이라, 한쪽 스키마가 바뀔 때 다른 쪽
 * 계약이 함께 끌려가지 않게 분리한다(도메인별 response 소유 규칙).
 */
@Schema(description = "행정동 경계 좌표 한 점")
public record AdminDongPointResponse(
    @Schema(description = "위도", example = "37.500123")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.036456")
    BigDecimal longitude
) {

    public static AdminDongPointResponse from(
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        return new AdminDongPointResponse(
            latitude,
            longitude
        );
    }
}
