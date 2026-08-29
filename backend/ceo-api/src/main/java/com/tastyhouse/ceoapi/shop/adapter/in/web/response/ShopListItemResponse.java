package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 가게 목록 항목 응답")
public record ShopListItemResponse(
    @Schema(description = "가게 ID", example = "1")
    Long id,

    @Schema(description = "상호명", example = "맛있는 분식")
    String name,

    @Schema(description = "지하철역명", example = "강남역")
    String stationName,

    @Schema(description = "도로명 주소", example = "서울시 강남구 테헤란로 1")
    String roadAddress,

    @Schema(description = "평균 평점", example = "4.5")
    Double rating,

    @Schema(description = "폐업 여부", example = "false")
    boolean permanentlyClosed
) {
    public static ShopListItemResponse from(
        Long id,
        String name,
        String stationName,
        String roadAddress,
        Double rating,
        boolean permanentlyClosed
    ) {
        return new ShopListItemResponse(
            id,
            name,
            stationName,
            roadAddress,
            rating,
            permanentlyClosed
        );
    }
}
