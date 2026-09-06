package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryTipRegionsUpdateCommand(
    Long ceoId,
    Long shopId,
    List<ShopDeliveryTipRegionCommand> regions
) {
    public ShopDeliveryTipRegionsUpdateCommand {
        if (ceoId == null || shopId == null || regions == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
