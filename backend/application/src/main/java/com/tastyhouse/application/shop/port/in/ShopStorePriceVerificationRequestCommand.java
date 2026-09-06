package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopStorePriceVerificationRequestCommand(
    Long ceoId,
    Long shopId,
    String items
) {
    public ShopStorePriceVerificationRequestCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
