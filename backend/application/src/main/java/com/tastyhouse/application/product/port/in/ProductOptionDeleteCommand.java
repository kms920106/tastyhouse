package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionDeleteCommand(
    Long ceoId,
    Long optionId,
    Long shopId
) {
    public ProductOptionDeleteCommand {
        if (ceoId == null
            || optionId == null
            || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
