package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopConvenienceInfoResult;

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
    public static ShopConvenienceInfoResponse from(ShopConvenienceInfoResult result) {
        return new ShopConvenienceInfoResponse(
            result.id(),
            result.shopId(),
            result.parkingAvailable(),
            result.parkingPaid(),
            result.valetAvailable(),
            result.valetPaid(),
            result.directionsGuide(),
            result.displayLatitude(),
            result.displayLongitude()
        );
    }

    /**
     * 편의정보를 아직 등록하지 않은 가게의 응답. 미등록을 {@code data: null}이 아니라 전 항목이 꺼진
     * 객체로 내려, 프론트가 두 가지 빈 상태(미등록 / 응답 없음)를 구분하지 않게 한다
     * (챕터 09에서 QueryService의 기본값 조립을 이 표현 계약으로 옮겼다).
     */
    public static ShopConvenienceInfoResponse empty(Long shopId) {
        return new ShopConvenienceInfoResponse(
            null,
            shopId,
            false,
            false,
            false,
            false,
            null,
            null,
            null
        );
    }
}
