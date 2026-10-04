package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopChoiceUpdateCommand(
    Long choiceId,
    String title,
    String content
) {

    public ShopChoiceUpdateCommand {
        if (choiceId == null || title == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
