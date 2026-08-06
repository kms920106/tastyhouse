package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 거리별 추가 배달팁 설정.
 *
 * <p>{@code extraTipType}이 {@code DISTANCE}인 가게에만 값이 있고, 그 외에는 응답의 이 필드가
 * {@code null}이다(거리별과 지역별은 상호 배타).
 */
@Schema(description = "거리별 추가 배달팁 설정")
public record ShopDeliveryTipDistanceItem(
    @Schema(description = "기본배달거리(m). 이 거리까지는 할증이 없습니다.", example = "2000")
    int baseDistanceMeters,

    @Schema(description = "할증 단위(PER_100M: 100m당, PER_500M: 500m당)", example = "PER_500M")
    String surchargeUnit,

    @Schema(description = "단위 거리당 할증액(원)", example = "500")
    int surchargeAmount
) {
    public static ShopDeliveryTipDistanceItem from(
        int baseDistanceMeters,
        String surchargeUnit,
        int surchargeAmount
    ) {
        return new ShopDeliveryTipDistanceItem(
            baseDistanceMeters,
            surchargeUnit,
            surchargeAmount
        );
    }
}
