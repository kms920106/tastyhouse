package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopAmenityCategoryUpdateCommand(
    Long categoryId,
    String displayName,
    Long activeImageFileId,
    Long inactiveImageFileId,
    Integer sort,
    Boolean visible
) {
    public ShopAmenityCategoryUpdateCommand {
        if (categoryId == null || displayName == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
