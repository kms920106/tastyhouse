package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record TagCreateCommand(
    String tagName
) {
    public TagCreateCommand {
        if (tagName == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
