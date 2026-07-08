package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지하철역 목록 항목 응답")
public record StationListItemResponse(
    @Schema(description = "지하철역 ID", example = "1")
    Long id,

    @Schema(description = "지하철역명", example = "강남역")
    String name
) {
    public static StationListItemResponse from(
        Long id,
        String name
    ) {
        return new StationListItemResponse(
            id,
            name
        );
    }
}
