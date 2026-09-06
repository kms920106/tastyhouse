package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryTipDistanceRemoveCommand(
    Long ceoId,
    Long shopId
) {
    public ShopDeliveryTipDistanceRemoveCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipDistanceRemoveCommand of(Long ceoId, Long shopId) {
        return new ShopDeliveryTipDistanceRemoveCommand(ceoId, shopId);
    }
}
