package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipSettingResult;

@Schema(description = "거리별 추가 배달팁 설정")
public record ShopDeliveryTipDistanceItem(
    @Schema(description = "기본배달거리(m). 이 거리까지는 할증이 없습니다.", example = "2000")
    int baseDistanceMeters,

    @Schema(description = "할증 단위(PER_100M: 100m당, PER_500M: 500m당)", example = "PER_500M")
    String surchargeUnit,

    @Schema(description = "단위 거리당 할증액(원)", example = "500")
    int surchargeAmount
) {
    public static ShopDeliveryTipDistanceItem from(ShopDeliveryTipSettingResult result) {
        return new ShopDeliveryTipDistanceItem(
            result.baseDistanceMeters(),
            result.surchargeUnit(),
            result.surchargeAmount()
        );
    }
}
