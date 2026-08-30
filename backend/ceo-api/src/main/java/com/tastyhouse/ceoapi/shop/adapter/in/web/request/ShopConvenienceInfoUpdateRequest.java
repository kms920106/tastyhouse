package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapplication.shop.port.in.ShopConvenienceInfoUpdateCommand;

@Schema(description = "내 가게 편의정보 등록/수정 요청")
public record ShopConvenienceInfoUpdateRequest(
    @NotNull(message = "주차 가능 여부는 필수입니다.")
    @Schema(description = "주차 가능 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean parkingAvailable,

    @NotNull(message = "주차 유료 여부는 필수입니다.")
    @Schema(description = "주차 유료 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean parkingPaid,

    @NotNull(message = "발렛 가능 여부는 필수입니다.")
    @Schema(description = "발렛 가능 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean valetAvailable,

    @NotNull(message = "발렛 유료 여부는 필수입니다.")
    @Schema(description = "발렛 유료 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean valetPaid,

    @Schema(description = "찾아오는길 안내", example = "2번 출구에서 도보 5분")
    String directionsGuide,

    @Schema(description = "노출 위치 위도 (가게 실제 위치 기준 1km 이내)", example = "37.497942")
    BigDecimal displayLatitude,

    @Schema(description = "노출 위치 경도 (가게 실제 위치 기준 1km 이내)", example = "127.027621")
    BigDecimal displayLongitude
) {

    public ShopConvenienceInfoUpdateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopConvenienceInfoUpdateCommand(
            ceoId,
            shopId,
            parkingAvailable(),
            parkingPaid(),
            valetAvailable(),
            valetPaid(),
            directionsGuide(),
            displayLatitude(),
            displayLongitude()
        );
    }
}
