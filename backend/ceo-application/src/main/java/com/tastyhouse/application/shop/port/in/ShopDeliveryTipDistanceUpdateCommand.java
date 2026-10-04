package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryTipDistanceUpdateCommand(
    Long ceoId,
    Long shopId,
    Integer baseDistanceMeters,
    String surchargeUnit,
    Integer surchargeAmount
) {

    public ShopDeliveryTipDistanceUpdateCommand {
        if (ceoId == null || shopId == null || surchargeUnit == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
