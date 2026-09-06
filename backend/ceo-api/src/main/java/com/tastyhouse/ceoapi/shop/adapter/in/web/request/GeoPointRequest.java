package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.shop.port.in.GeoPointCommand;

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
