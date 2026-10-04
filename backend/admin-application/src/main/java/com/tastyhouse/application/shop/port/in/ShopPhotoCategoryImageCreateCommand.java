package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopPhotoCategoryImageCreateCommand(
    Long categoryId,
    Long imageFileId,
    Integer sort,
    Boolean visible
) {

    public ShopPhotoCategoryImageCreateCommand {
        if (categoryId == null || imageFileId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
