package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopPhotoCategoryImageDeleteCommand(
    Long imageId
) {

    public ShopPhotoCategoryImageDeleteCommand {
        if (imageId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopPhotoCategoryImageDeleteCommand of(Long imageId) {
        return new ShopPhotoCategoryImageDeleteCommand(imageId);
    }
}
