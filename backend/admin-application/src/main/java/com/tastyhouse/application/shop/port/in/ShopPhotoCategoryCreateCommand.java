package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopPhotoCategoryCreateCommand(
    Long shopId,
    String name
) {

    public ShopPhotoCategoryCreateCommand {
        if (shopId == null || name == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
