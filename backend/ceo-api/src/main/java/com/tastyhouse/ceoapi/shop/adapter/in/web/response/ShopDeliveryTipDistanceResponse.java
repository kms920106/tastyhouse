package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "거리별 추가 배달팁 설정")
public record ShopDeliveryTipDistanceResponse(
    @Schema(description = "기본배달거리(m). 이 거리까지는 할증이 없습니다", example = "1500")
    int baseDistanceMeters,

    @Schema(description = "할증 단위", example = "PER_500M", allowableValues = {"PER_100M", "PER_500M"})
    String surchargeUnit,

    @Schema(description = "단위당 할증액(원)", example = "500")
    int surchargeAmount
) {
    public static ShopDeliveryTipDistanceResponse from(
        int baseDistanceMeters,
        String surchargeUnit,
        int surchargeAmount
    ) {
        return new ShopDeliveryTipDistanceResponse(
            baseDistanceMeters,
            surchargeUnit,
            surchargeAmount
        );
    }
}
