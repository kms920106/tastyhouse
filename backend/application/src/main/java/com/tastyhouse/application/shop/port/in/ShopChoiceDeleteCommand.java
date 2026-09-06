package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopChoiceDeleteCommand(
    Long choiceId
) {
    public ShopChoiceDeleteCommand {
        if (choiceId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopChoiceDeleteCommand of(Long choiceId) {
        return new ShopChoiceDeleteCommand(choiceId);
    }
}
