package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopContentBoardHiddenChangeCommand(
    Long contentBoardId,
    Boolean hidden
) {
    public ShopContentBoardHiddenChangeCommand {
        if (contentBoardId == null || hidden == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
