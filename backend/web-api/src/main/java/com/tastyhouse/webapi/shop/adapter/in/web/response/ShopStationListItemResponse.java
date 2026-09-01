package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.StationResult;

@Schema(description = "지하철역 목록 항목 응답")
public record ShopStationListItemResponse(
    @Schema(description = "지하철역 ID", example = "1")
    Long id,

    @Schema(description = "지하철역명", example = "강남역")
    String name
) {
    public static ShopStationListItemResponse from(StationResult result) {
        return new ShopStationListItemResponse(
            result.id(),
            result.stationName()
        );
    }
}
