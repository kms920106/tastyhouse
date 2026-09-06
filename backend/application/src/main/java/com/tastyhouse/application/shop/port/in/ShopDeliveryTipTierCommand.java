package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryTipTierCommand(
    Integer minOrderAmount,
    Integer tipAmount
) {
    public ShopDeliveryTipTierCommand {
        if (minOrderAmount == null || tipAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipTierCommand of(Integer minOrderAmount, Integer tipAmount) {
        return new ShopDeliveryTipTierCommand(minOrderAmount, tipAmount);
    }
}
