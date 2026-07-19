package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지하철역 응답")
public record StationResponse(
    @Schema(description = "지하철역 ID", example = "1")
    Long id,

    @Schema(description = "지하철역 이름", example = "강남역")
    String stationName
) {
    public static StationResponse from(Long id, String stationName) {
        return new StationResponse(id, stationName);
    }
}
