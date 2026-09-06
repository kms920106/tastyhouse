package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopPhotoCategoryImageDeleteCommand(
    Long imageId
) {
    public ShopPhotoCategoryImageDeleteCommand {
        if (imageId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopPhotoCategoryImageDeleteCommand of(Long imageId) {
        return new ShopPhotoCategoryImageDeleteCommand(imageId);
    }
}
