package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;

/**
 * 배달팁 설정 헤더(표현용).
 *
 * <p>거리별 설정 3필드는 {@code extraTipType}이 {@link DeliveryTipExtraType#DISTANCE}일 때만 값이 있다.
 * 설정 헤더 행 자체가 없는 가게(배달팁 미설정)는 이 Result가 아예 조회되지 않는다.
 */
public record ShopDeliveryTipSettingResult(
    Long id,
    String extraTipType,
    Integer baseDistanceMeters,
    String surchargeUnit,
    Integer surchargeAmount
) {
}
