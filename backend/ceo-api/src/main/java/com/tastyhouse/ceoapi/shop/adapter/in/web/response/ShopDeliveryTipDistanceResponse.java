package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipSettingResult;

@Schema(description = "거리별 추가 배달팁 설정")
public record ShopDeliveryTipDistanceResponse(
    @Schema(description = "기본배달거리(m). 이 거리까지는 할증이 없습니다", example = "1500")
    int baseDistanceMeters,

    @Schema(description = "할증 단위", example = "PER_500M", allowableValues = {"PER_100M", "PER_500M"})
    String surchargeUnit,

    @Schema(description = "단위당 할증액(원)", example = "500")
    int surchargeAmount
) {
    private static final String EXTRA_TIP_TYPE_DISTANCE = "DISTANCE";

    public static ShopDeliveryTipDistanceResponse from(ShopDeliveryTipSettingResult result) {
        if (result == null || !EXTRA_TIP_TYPE_DISTANCE.equals(result.extraTipType())) {
            return null;
        }
        return new ShopDeliveryTipDistanceResponse(
            result.baseDistanceMeters(),
            result.surchargeUnit(),
            result.surchargeAmount()
        );
    }
}
