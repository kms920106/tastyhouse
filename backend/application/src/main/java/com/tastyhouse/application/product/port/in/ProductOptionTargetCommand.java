package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionTargetCommand(
    Long optionId,
    String optionType
) {
    public ProductOptionTargetCommand {
        if (optionId == null
            || optionType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
