package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopPhotoCategoryImageUpdateCommand(
    Long imageId,
    Long imageFileId,
    Integer sort,
    Boolean visible
) {

    public ShopPhotoCategoryImageUpdateCommand {
        if (imageId == null || imageFileId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
