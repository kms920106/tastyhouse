package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopRiderPickupLocationClearCommand(
    Long ceoId,
    Long shopId
) {
    public ShopRiderPickupLocationClearCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopRiderPickupLocationClearCommand of(Long ceoId, Long shopId) {
        return new ShopRiderPickupLocationClearCommand(ceoId, shopId);
    }
}
