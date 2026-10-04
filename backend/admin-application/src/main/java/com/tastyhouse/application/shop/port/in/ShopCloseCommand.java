package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopCloseCommand(
    Long shopId
) {

    public ShopCloseCommand {
        if (shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopCloseCommand of(Long shopId) {
        return new ShopCloseCommand(shopId);
    }
}
