package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopPhotoCategoryDeleteCommand(
    Long categoryId
) {

    public ShopPhotoCategoryDeleteCommand {
        if (categoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopPhotoCategoryDeleteCommand of(Long categoryId) {
        return new ShopPhotoCategoryDeleteCommand(categoryId);
    }
}
