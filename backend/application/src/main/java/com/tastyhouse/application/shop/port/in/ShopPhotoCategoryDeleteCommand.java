package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopPhotoCategoryDeleteCommand(
    Long categoryId
) {
    public ShopPhotoCategoryDeleteCommand {
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopPhotoCategoryDeleteCommand of(Long categoryId) {
        return new ShopPhotoCategoryDeleteCommand(categoryId);
    }
}
