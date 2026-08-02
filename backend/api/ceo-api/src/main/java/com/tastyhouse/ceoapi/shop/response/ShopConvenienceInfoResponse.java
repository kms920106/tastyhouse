package com.tastyhouse.ceoapi.shop.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 가게 편의정보 응답")
public record ShopConvenienceInfoResponse(
    @Schema(description = "편의정보 ID (등록 이력이 없으면 null)", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "주차 가능 여부", example = "true")
    boolean parkingAvailable,

    @Schema(description = "주차 유료 여부", example = "false")
    boolean parkingPaid,

    @Schema(description = "발렛 가능 여부", example = "false")
    boolean valetAvailable,

    @Schema(description = "발렛 유료 여부", example = "false")
    boolean valetPaid,

    @Schema(description = "찾아오는길 안내", example = "2번 출구에서 도보 5분")
    String directionsGuide,

    @Schema(description = "노출 위치 위도", example = "37.497942")
    BigDecimal displayLatitude,

    @Schema(description = "노출 위치 경도", example = "127.027621")
    BigDecimal displayLongitude
) {
    public static ShopConvenienceInfoResponse from(
        Long id,
        Long shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        return new ShopConvenienceInfoResponse(
            id,
            shopId,
            parkingAvailable,
            parkingPaid,
            valetAvailable,
            valetPaid,
            directionsGuide,
            displayLatitude,
            displayLongitude
        );
    }
}
