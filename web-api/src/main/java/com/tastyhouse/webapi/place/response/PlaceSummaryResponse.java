package com.tastyhouse.webapi.place.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이스 요약 정보 응답")
public record PlaceSummaryResponse(
    @Schema(description = "플레이스 ID", example = "1")
    Long id,

    @Schema(description = "상호명", example = "리틀넥 청담")
    String name,

    @Schema(description = "도로명 주소", example = "서울 강남구 도산대로51길 17")
    String roadAddress,

    @Schema(description = "지번 주소", example = "서울 강남구 신사동 653-7")
    String lotAddress,

    @Schema(description = "총 평점", example = "4.8")
    Double rating
) {
    public static PlaceSummaryResponse from(
    Long id,
    String name,
    String roadAddress,
    String lotAddress,
    Double rating
    ) {
    return new PlaceSummaryResponse(
        id,
        name,
        roadAddress,
        lotAddress,
        rating
    );
    }
}
