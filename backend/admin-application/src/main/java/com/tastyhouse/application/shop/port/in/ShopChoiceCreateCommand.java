package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopChoiceCreateCommand(
    Long shopId,
    String title,
    String content
) {

    public ShopChoiceCreateCommand {
        if (shopId == null || title == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
