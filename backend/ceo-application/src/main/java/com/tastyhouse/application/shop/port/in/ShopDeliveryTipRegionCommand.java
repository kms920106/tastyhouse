package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryTipRegionCommand(
    Long adminDongId,
    Integer tipAmount
) {

    public ShopDeliveryTipRegionCommand {
        if (adminDongId == null || tipAmount == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipRegionCommand of(Long adminDongId, Integer tipAmount) {
        return new ShopDeliveryTipRegionCommand(adminDongId, tipAmount);
    }
}
