package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryTipTiersUpdateCommand(
    Long ceoId,
    Long shopId,
    List<ShopDeliveryTipTierCommand> tiers
) {
    public ShopDeliveryTipTiersUpdateCommand {
        if (ceoId == null || shopId == null || tiers == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
