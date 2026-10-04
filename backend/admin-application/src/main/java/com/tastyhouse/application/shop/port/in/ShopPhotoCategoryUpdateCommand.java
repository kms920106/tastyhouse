package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopPhotoCategoryUpdateCommand(
    Long categoryId,
    String name
) {

    public ShopPhotoCategoryUpdateCommand {
        if (categoryId == null || name == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
