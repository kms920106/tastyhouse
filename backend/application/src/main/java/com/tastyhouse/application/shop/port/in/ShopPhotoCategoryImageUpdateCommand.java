package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopPhotoCategoryImageUpdateCommand(
    Long imageId,
    Long imageFileId,
    Integer sort,
    Boolean visible
) {
    public ShopPhotoCategoryImageUpdateCommand {
        if (imageId == null || imageFileId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
