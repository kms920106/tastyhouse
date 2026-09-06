package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopCloseCommand(
    Long shopId
) {
    public ShopCloseCommand {
        if (shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopCloseCommand of(Long shopId) {
        return new ShopCloseCommand(shopId);
    }
}
