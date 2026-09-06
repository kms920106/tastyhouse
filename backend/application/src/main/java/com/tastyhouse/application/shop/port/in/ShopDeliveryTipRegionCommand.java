package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryTipRegionCommand(
    Long adminDongId,
    Integer tipAmount
) {
    public ShopDeliveryTipRegionCommand {
        if (adminDongId == null || tipAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipRegionCommand of(Long adminDongId, Integer tipAmount) {
        return new ShopDeliveryTipRegionCommand(adminDongId, tipAmount);
    }
}
