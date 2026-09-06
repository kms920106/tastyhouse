package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopChoiceCreateCommand(
    Long shopId,
    String title,
    String content
) {
    public ShopChoiceCreateCommand {
        if (shopId == null || title == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
