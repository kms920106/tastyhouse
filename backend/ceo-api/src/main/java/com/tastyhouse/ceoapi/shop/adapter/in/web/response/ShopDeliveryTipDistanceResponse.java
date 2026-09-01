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
    /** 추가팁 유형이 거리별일 때만 값이 성립한다. 그 밖에는 응답의 {@code distance}를 비운다. */
    private static final String EXTRA_TIP_TYPE_DISTANCE = "DISTANCE";

    /**
     * 거리별 설정 3필드를 응답으로 조립한다 — 거리별을 쓰지 않는 가게는 세 값이 모두 비어 있으므로
     * {@code null}을 반환해 응답의 {@code distance} 필드를 비운다(챕터 09에서 QueryService의 private
     * 매퍼를 이 표현 계약으로 옮겼다).
     */
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
