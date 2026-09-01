package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipSettingResult;

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
    /**
     * 거리별 설정 3필드를 옮긴다 — 거리별을 쓰는 가게만 이 자리에 오므로({@code null}이면 상위 응답의
     * {@code distance}가 비어 있다) 세 필드가 모두 채워져 있음이 보장된다. 그 판정은 서비스가 한다.
     */
    public static ShopDeliveryTipDistanceItem from(ShopDeliveryTipSettingResult result) {
        return new ShopDeliveryTipDistanceItem(
            result.baseDistanceMeters(),
            result.surchargeUnit(),
            result.surchargeAmount()
        );
    }
}
