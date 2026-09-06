package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopPhotoCategoryCreateCommand(
    Long shopId,
    String name
) {
    public ShopPhotoCategoryCreateCommand {
        if (shopId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
