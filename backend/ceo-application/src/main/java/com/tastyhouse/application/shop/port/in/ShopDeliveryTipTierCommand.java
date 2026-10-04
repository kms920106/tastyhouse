package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryTipTierCommand(
    Integer minOrderAmount,
    Integer tipAmount
) {

    public ShopDeliveryTipTierCommand {
        if (minOrderAmount == null || tipAmount == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipTierCommand of(Integer minOrderAmount, Integer tipAmount) {
        return new ShopDeliveryTipTierCommand(minOrderAmount, tipAmount);
    }
}
