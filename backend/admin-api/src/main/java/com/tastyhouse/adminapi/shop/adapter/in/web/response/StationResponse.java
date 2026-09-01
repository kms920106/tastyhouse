package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.StationResult;

@Schema(description = "지하철역 응답")
public record StationResponse(
    @Schema(description = "지하철역 ID", example = "1")
    Long id,

    @Schema(description = "지하철역 이름", example = "강남역")
    String stationName
) {
    public static StationResponse from(StationResult result) {
        return new StationResponse(
            result.id(),
            result.stationName()
        );
    }
}
