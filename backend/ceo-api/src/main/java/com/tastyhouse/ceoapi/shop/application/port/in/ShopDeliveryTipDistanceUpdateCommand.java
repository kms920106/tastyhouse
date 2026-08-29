package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 거리별 추가 배달팁 설정 command.
 */
public record ShopDeliveryTipDistanceUpdateCommand(
    Long ceoId,
    Long shopId,
    Integer baseDistanceMeters,
    String surchargeUnit,
    Integer surchargeAmount
) {
    public ShopDeliveryTipDistanceUpdateCommand {
        if (ceoId == null || shopId == null || surchargeUnit == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
