package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
