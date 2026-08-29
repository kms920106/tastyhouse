package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapi.shop.application.port.in.GeoPointCommand;

/**
 * 도형을 이루는 좌표 한 점(요청 경계).
 *
 * <p><b>{@code [경도, 위도]} 배열이 아니라 이름 있는 객체로 받는다.</b> GeoJSON은 배열의 0번이 경도인데
 * 지도 SDK·사람의 직관은 대개 "위도, 경도" 순서라, 배열로 주고받으면 순서를 뒤집어 보내도 값이 유효
 * 범위 안이면 검증을 통과하고 <b>엉뚱한 곳에 배달지역이 그려진다</b>. 필드명을 붙이면 그 실수가 불가능하다.
 */
@Schema(description = "좌표 한 점")
public record GeoPointRequest(
    @NotNull(message = "위도는 필수입니다.")
    @DecimalMin(value = "-90", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90", message = "위도는 90 이하여야 합니다.")
    @Schema(description = "위도", example = "37.500000", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal latitude,

    @NotNull(message = "경도는 필수입니다.")
    @DecimalMin(value = "-180", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180", message = "경도는 180 이하여야 합니다.")
    @Schema(description = "경도", example = "127.036000", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal longitude
) {

    public GeoPointCommand toCommand() {
        return new GeoPointCommand(latitude(), longitude());
    }
}
