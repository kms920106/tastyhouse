package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryTipRegionsRemoveCommand(
    Long ceoId,
    Long shopId
) {
    public ShopDeliveryTipRegionsRemoveCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipRegionsRemoveCommand of(Long ceoId, Long shopId) {
        return new ShopDeliveryTipRegionsRemoveCommand(ceoId, shopId);
    }
}
