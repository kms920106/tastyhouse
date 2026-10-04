package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopFoodTypeCategoryUpdateCommand(
    Long categoryId,
    String displayName,
    Long activeImageFileId,
    Long inactiveImageFileId,
    Integer sort,
    Boolean visible
) {

    public ShopFoodTypeCategoryUpdateCommand {
        if (categoryId == null || displayName == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
