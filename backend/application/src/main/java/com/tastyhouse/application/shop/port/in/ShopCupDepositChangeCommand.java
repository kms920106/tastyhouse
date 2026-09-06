package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopCupDepositChangeCommand(
    Long shopId,
    Boolean enabled
) {
    public ShopCupDepositChangeCommand {
        if (shopId == null || enabled == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
