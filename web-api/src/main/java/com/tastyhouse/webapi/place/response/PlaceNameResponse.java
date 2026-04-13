package com.tastyhouse.webapi.place.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이스 이름 조회 응답")
public record PlaceNameResponse(
    @Schema(description = "플레이스 ID", example = "1")
    Long id,

    @Schema(description = "상호명", example = "리틀넥 청담")
    String name
) {
    public static PlaceNameResponse from(
        Long id,
        String name
    ) {
        return new PlaceNameResponse(
            id,
            name
        );
    }
}
