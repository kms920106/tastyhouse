package com.tastyhouse.ceoapi.region.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.region.port.out.AdminDongBoundaryViewResult;

@Schema(description = "행정동 경계 좌표 한 점")
public record AdminDongPointResponse(
    @Schema(description = "위도", example = "37.500123")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.036456")
    BigDecimal longitude
) {
    public static AdminDongPointResponse from(AdminDongBoundaryViewResult.Point point) {
        return new AdminDongPointResponse(
            point.latitude(),
            point.longitude()
        );
    }
}
