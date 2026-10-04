package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopContentBoardHiddenChangeCommand(
    Long contentBoardId,
    Boolean hidden
) {

    public ShopContentBoardHiddenChangeCommand {
        if (contentBoardId == null || hidden == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
