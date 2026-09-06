package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionGroupDeleteCommand(
    Long ceoId,
    Long optionGroupId,
    Long shopId
) {
    public ProductOptionGroupDeleteCommand {
        if (ceoId == null
            || optionGroupId == null
            || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
