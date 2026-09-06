package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopPhotoCategoryImageCreateCommand(
    Long categoryId,
    Long imageFileId,
    Integer sort,
    Boolean visible
) {
    public ShopPhotoCategoryImageCreateCommand {
        if (categoryId == null || imageFileId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
