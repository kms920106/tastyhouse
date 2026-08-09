package com.tastyhouse.ceoapi.region.request;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 행정동 경계 조회 요청.
 *
 * <p>조회 방식이 두 가지이며 <b>서로 배타적</b>이다 — 지도 영역(bbox)으로 훑거나, 식별자를 지정해 특정
 * 동만 다시 그리거나. 둘 다 비면 전국을 요청하는 것이 되어 거절한다. 이 배타성 검증은 서비스가 수행한다.
 */
@Schema(description = "행정동 경계 조회 요청")
public record AdminDongBoundarySearchRequest(
    @DecimalMin(value = "-90", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90", message = "위도는 90 이하여야 합니다.")
    @Schema(description = "남서쪽 위도", example = "37.480000")
    BigDecimal swLat,

    @DecimalMin(value = "-180", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180", message = "경도는 180 이하여야 합니다.")
    @Schema(description = "남서쪽 경도", example = "127.010000")
    BigDecimal swLng,

    @DecimalMin(value = "-90", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90", message = "위도는 90 이하여야 합니다.")
    @Schema(description = "북동쪽 위도", example = "37.520000")
    BigDecimal neLat,

    @DecimalMin(value = "-180", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180", message = "경도는 180 이하여야 합니다.")
    @Schema(description = "북동쪽 경도", example = "127.060000")
    BigDecimal neLng,

    @Min(value = 1, message = "줌 레벨은 1 이상이어야 합니다.")
    @Max(value = 14, message = "줌 레벨은 14 이하여야 합니다.")
    @Schema(description = "지도 줌 레벨(1~14). 서버가 이 값으로 응답 규모를 조절", example = "8")
    Integer level,

    @Size(max = 200, message = "한 번에 조회할 수 있는 행정동은 최대 200개입니다.")
    @Schema(description = "행정동 ID 목록(bbox와 배타적)")
    List<Long> adminDongIds
) {
}
