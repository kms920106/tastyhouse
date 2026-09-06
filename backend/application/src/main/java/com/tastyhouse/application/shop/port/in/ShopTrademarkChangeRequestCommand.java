package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopTrademarkChangeRequestCommand(
    Long ceoId,
    Long shopId
) {
    public ShopTrademarkChangeRequestCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopTrademarkChangeRequestCommand of(Long ceoId, Long shopId) {
        return new ShopTrademarkChangeRequestCommand(ceoId, shopId);
    }
}
