package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductCategoryDeleteCommand(
    Long ceoId,
    Long productCategoryId,
    Long shopId
) {
    public ProductCategoryDeleteCommand {
        if (ceoId == null
            || productCategoryId == null
            || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
